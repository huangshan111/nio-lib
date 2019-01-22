package com.jd.nio.impl.async;

import com.jd.nio.IoArgs;
import com.jd.nio.ReceiveDispatcher;
import com.jd.nio.ReceivePacket;
import com.jd.nio.Receiver;
import com.jd.nio.impl.box.StringReceivePacket;
import com.jd.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by huangshan11 on 2018/12/19.
 */
public class AsyncReceiveDispatcher implements ReceiveDispatcher,IoArgs.IoArgsProcesser{

    private Receiver receiver;
    private AtomicBoolean isClosed = new AtomicBoolean(false);
    private ReceivePacketCallback callback;
    private IoArgs ioArgs = new IoArgs();
    private ReceivePacket packetTemp;
    private WritableByteChannel packetChannel;
    private long total;
    private long position;

    public AsyncReceiveDispatcher(Receiver receiver, ReceivePacketCallback callback) {
        receiver.setReceiveEventListener(this);
        this.receiver = receiver;
        this.callback = callback;
    }

    @Override
    public void start() {
        if (isClosed.compareAndSet(false, true)) {
            registerReceive();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            ReceivePacket packet = packetTemp;
            if (packet != null) {
                CloseUtils.close(packet);
                packetTemp = null;
            }
        }
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    private void registerReceive() {
        try {
            receiver.postReceiveAsync();
        } catch (IOException e) {
            closeAndNotify();
        }
    }

    private final IoArgs.IoArgsEventListener receiveEventListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStarted(IoArgs args) {
            int size = 0;
            if (packetTemp == null) {
                size = 4;
            } else {
                size = Math.min(total - position, args.capacity());
            }
            args.limit(size);
        }

        @Override
        public void onCompleted(IoArgs args) {
            assemblePacket(args);
            registerReceive();
        }
    };

    private void assemblePacket(IoArgs args) {
        if (packetTemp == null) {
            int length = args.readLength();
            packetTemp = new StringReceivePacket(length);
            buffer = new byte[length];
            total = length;
            position = 0;
        }

        int count = args.writeTo(buffer, 0);
        if (count > 0) {
            packetTemp.save(buffer, count);
            position += count;
            if (position == total) {
                completePacket();
                packetTemp = null;
            }
        }
    }

    private void completePacket(boolean isSucceed) {
        ReceivePacket packet = packetTemp;
        CloseUtils.close(packet);
        callback.onReceivePacketCompleted(packet);
    }

    @Override
    public IoArgs provideIoArgs() {
        IoArgs args = ioArgs;
        int size = 0;
        if (packetTemp == null) {
            size = 4;
        } else {
            size = (int) Math.min(total - position, args.capacity());
        }
        args.limit(size);
        return args;
    }

    @Override
    public void onConsumerSuccess(IoArgs args) {

    }

    @Override
    public void onConsumerFail(IoArgs args, IOException ex) {

    }
}
