package com.jd.nio;

import com.jd.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by huangshan11 on 2018/12/17.
 */
public class IoSelectorProvider implements IoProvider {

    private AtomicBoolean isClosed = new AtomicBoolean(false);

    private AtomicBoolean inRegInput = new AtomicBoolean(false);
    private AtomicBoolean inRegOutput = new AtomicBoolean(false);

    private Selector readSelector;
    private Selector writeSelector;

    private Map<SelectionKey, Runnable> inputCallbackMap = new HashMap<>();
    private Map<SelectionKey, Runnable> outputCallbackMap = new HashMap<>();

    private ExecutorService inputThreadPool;
    private ExecutorService outputThreadPool;

    public IoSelectorProvider() throws IOException {

        readSelector = Selector.open();
        writeSelector = Selector.open();

        inputThreadPool = Executors.newFixedThreadPool(4, new IoProviderThreadFactory("IoProvider-Input-Thread-"));
        outputThreadPool = Executors.newFixedThreadPool(4, new IoProviderThreadFactory("IoProvider-Output-Thread-"));

        startRead();
        startWrite();
    }

    private void startRead() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (!isClosed.get()) {
                    try {
                        if (readSelector.select() == 0) {
                            waitSelection(inRegInput);
                            continue;
                        }

                        Set<SelectionKey> keys = readSelector.selectedKeys();
                        for (SelectionKey key : keys) {
                            if (key.isValid()) {
                                handleSelection(key, inputCallbackMap, inputThreadPool);
                            }
                        }
                        keys.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "io-provider-read-selector");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private void startWrite() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (!isClosed.get()) {
                    try {
                        if (writeSelector.select() == 0) {
                            waitSelection(inRegOutput);
                            continue;
                        }

                        Set<SelectionKey> keys = writeSelector.selectedKeys();
                        for (SelectionKey key : keys) {
                            if (key.isValid()) {
                                handleSelection(key, outputCallbackMap, outputThreadPool);
                            }
                        }
                        keys.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "io-provider-write-selector");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    @Override
    public boolean registerInput(SocketChannel channel, HandleInputCallback callback) {
        return registerSelection(channel, readSelector, SelectionKey.OP_READ, inRegInput,
                inputCallbackMap, callback) != null;
    }

    @Override
    public boolean registerOutput(SocketChannel channel, HandleOutputCallback callback) {
        return registerSelection(channel, writeSelector, SelectionKey.OP_WRITE, inRegOutput,
                outputCallbackMap, callback) != null;
    }

    public void unRegisterInput(SocketChannel channel) {
        unRegisterSelection(channel, readSelector, inputCallbackMap);
    }

    @Override
    public void unRegisterOutput(SocketChannel channel) {
        unRegisterSelection(channel, writeSelector, outputCallbackMap);
    }

    private static SelectionKey registerSelection(SocketChannel channel, Selector selector, int registerOps, AtomicBoolean locker, Map<SelectionKey, Runnable> map, Runnable runnable) {
        synchronized (locker) {
            try {
                locker.set(true);
                selector.wakeup();
                SelectionKey key = channel.keyFor(selector);
                if (key != null) {
                    key.interestOps(key.readyOps() | registerOps);
                } else {
                    key = channel.register(selector, registerOps);
                    map.put(key, runnable);
                }
                return key;
            } catch (ClosedChannelException e) {
            } finally {
                locker.set(false);
                locker.notify();
                return null;
            }
        }
    }

    private static void unRegisterSelection(SocketChannel channel, Selector selector, Map<SelectionKey, Runnable> map) {
        if (channel.isRegistered()) {
            SelectionKey key = channel.keyFor(selector);
            if (key != null) {
                // 取消监听的方法
                key.cancel();
                map.remove(key);
                selector.wakeup();
            }
        }
    }

    private static void waitSelection(AtomicBoolean locker) {
        synchronized (locker) {
            if (locker.get()) {
                try {
                    locker.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static void handleSelection(SelectionKey key, Map<SelectionKey, Runnable> map, ExecutorService threadPool) {
        // 重点
        // 取消继续对keyOps的监听
        //key.interestOps(key.readyOps() & ~op);
        key.cancel();
        Runnable runnable = map.get(key);
        if (runnable != null && !threadPool.isShutdown()) {
            // 异步调度
            threadPool.execute(runnable);
        }
    }

    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {

            inputCallbackMap.clear();
            outputCallbackMap.clear();

            inputThreadPool.shutdown();
            outputThreadPool.shutdown();

            readSelector.wakeup();
            writeSelector.wakeup();

            CloseUtils.close(readSelector, writeSelector);
        }
    }
}
