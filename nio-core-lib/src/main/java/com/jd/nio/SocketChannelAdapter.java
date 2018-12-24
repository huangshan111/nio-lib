package com.jd.nio;

import com.jd.utils.CloseUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by huangshan11 on 2018/12/17.
 */
public class SocketChannelAdapter implements Sender, Receiver, Closeable {

    private AtomicBoolean isClosed = new AtomicBoolean(false);
    private SocketChannel channel;
    private IoProvider ioProvider;
    private OnChannelStatusChangedListener listener;

    private IoArgs ioArgsTemp;

    IoArgs.IoArgsEventListener sendEventListener;
    IoArgs.IoArgsEventListener receiveEventListener;

    public SocketChannelAdapter(SocketChannel channel, IoSelectorProvider ioProvider, OnChannelStatusChangedListener listener) throws IOException {
        this.channel = channel;
        this.ioProvider = ioProvider;
        this.listener = listener;
        channel.configureBlocking(false);
    }

    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            ioProvider.unRegisterInput(channel);
            ioProvider.unRegisterOutput(channel);
            CloseUtils.close(channel);
            listener.onChannelClosed(channel);
        }
    }

    @Override
    public boolean sendAsync(IoArgs args) throws IOException {
        if (isClosed.get()) {
            throw new IOException("当前的通道已经关闭");
        }
        outputCallback.setAttach(args);
        return ioProvider.registerOutput(channel, outputCallback);
    }

    @Override
    public boolean receiveAsync(IoArgs args) throws IOException {
        if (isClosed.get()) {
            throw new IOException("当前的通道已经关闭");
        }
        ioArgsTemp = args;
        return ioProvider.registerInput(channel, inputCallback);
    }

    @Override
    public void setReceiveEventListener(IoArgs.IoArgsEventListener listener) {
        receiveEventListener = listener;
    }

    @Override
    public void setSendEventListener(IoArgs.IoArgsEventListener ioArgsEventListener) {
        sendEventListener = ioArgsEventListener;
    }

    private final IoProvider.HandleInputCallback inputCallback = new IoProvider.HandleInputCallback() {

        @Override
        protected void canProviderInput() {
            if (isClosed.get()) {
                return;
            }

            IoArgs.IoArgsEventListener listener = SocketChannelAdapter.this.receiveEventListener;
            listener.onStarted(ioArgsTemp);
            try {
                // 具体的读取操作
                if (ioArgsTemp.readFrom(channel) > 0) {
                    // 读取完成回调
                    listener.onCompleted(ioArgsTemp);
                } else {
                    throw new IOException("不能读取数据了");
                }
            } catch (IOException ignored) {
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }

    };

    private final IoProvider.HandleOutputCallback outputCallback = new IoProvider.HandleOutputCallback() {
        @Override
        protected void canProviderOutput() {
            if (isClosed.get()) {
                return;
            }
            IoArgs.IoArgsEventListener listener = SocketChannelAdapter.this.sendEventListener;
            IoArgs args = getAttach();
            listener.onStarted(args);
            try {
                // 具体的读取操作
                if (args.writeTo(channel) > 0) {
                    // 读取完成回调
                    listener.onCompleted(args);
                } else {
                    throw new IOException("不能写数据了");
                }
            } catch (IOException ignored) {
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };
}
