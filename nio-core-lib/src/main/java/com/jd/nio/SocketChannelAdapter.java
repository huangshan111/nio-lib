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

    IoArgs.IoArgsProcesser sendProcesser;
    IoArgs.IoArgsProcesser receiveProcesser;

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
    public boolean postSendAsync() throws IOException {
        if (isClosed.get()) {
            throw new IOException("当前的通道已经关闭");
        }
        return ioProvider.registerOutput(channel, outputCallback);
    }

    @Override
    public boolean postReceiveAsync() throws IOException {
        if (isClosed.get()) {
            throw new IOException("当前的通道已经关闭");
        }
        return ioProvider.registerInput(channel, inputCallback);
    }

    @Override
    public void setReceiveEventListener(IoArgs.IoArgsProcesser processer) {
        receiveProcesser = processer;
    }

    @Override
    public void setSendEventListener(IoArgs.IoArgsProcesser processer) {
        this.sendProcesser = processer;
    }

    private final IoProvider.HandleInputCallback inputCallback = new IoProvider.HandleInputCallback() {

        @Override
        protected void canProviderInput() {
            if (isClosed.get()) {
                return;
            }

            IoArgs.IoArgsProcesser processer = SocketChannelAdapter.this.receiveProcesser;
            IoArgs ioArgs = processer.provideIoArgs();
            try {
                // 具体的读取操作
                if (ioArgs.readFrom(channel) > 0) {
                    // 读取完成回调
                    processer.onConsumerSuccess(ioArgs);
                } else {
                    processer.onConsumerFail(ioArgs, new IOException("不能读取数据了"));
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
            IoArgs.IoArgsProcesser processer = SocketChannelAdapter.this.sendProcesser;
            IoArgs ioArgs = processer.provideIoArgs();
            try {
                if (ioArgs.writeTo(channel) > 0) {
                    processer.onConsumerSuccess(ioArgs);
                } else {
                    processer.onConsumerFail(ioArgs, new IOException("不能写数据了"));
                }
            } catch (IOException ignored) {
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };
}
