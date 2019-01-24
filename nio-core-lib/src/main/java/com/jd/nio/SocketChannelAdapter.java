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

    IoArgs.IoArgsEventProcesser sendProcesser;
    IoArgs.IoArgsEventProcesser receiveProcesser;

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
    public void setReceiveEventListener(IoArgs.IoArgsEventProcesser processer) {
        this.receiveProcesser = processer;
    }

    @Override
    public void setSendEventListener(IoArgs.IoArgsEventProcesser processer) {
        this.sendProcesser = processer;
    }

    private final IoProvider.HandleInputCallback inputCallback = new IoProvider.HandleInputCallback() {

        @Override
        protected void canProviderInput() {
            if (isClosed.get()) {
                return;
            }

            IoArgs.IoArgsEventProcesser processer = SocketChannelAdapter.this.receiveProcesser;
            IoArgs args = processer.provideIoArgs();
            try {
                // 具体的读取操作
                if (args.readFrom(channel) > 0) {
                    // 读取完成回调
                    processer.onConsumeCompleted(args);
                } else {
                    processer.onConsumeFailed(args, new IOException("不能读取数据了"));
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
            IoArgs.IoArgsEventProcesser processer = SocketChannelAdapter.this.sendProcesser;
            IoArgs args = processer.provideIoArgs();
            try {
                if (args.writeTo(channel) > 0) {
                    processer.onConsumeCompleted(args);
                } else {
                    processer.onConsumeFailed(args, new IOException("不能写数据了"));
                }
            } catch (IOException ignored) {
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };
}
