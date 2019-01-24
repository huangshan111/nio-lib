package com.jd.nio.impl.async;

import com.jd.nio.IoArgs;
import com.jd.nio.ReceiveDispatcher;
import com.jd.nio.ReceivePacket;
import com.jd.nio.Receiver;
import com.jd.nio.impl.box.StringReceivePacket;
import com.jd.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by huangshan11 on 2018/12/19.
 */
public class AsyncReceiveDispatcher implements ReceiveDispatcher, IoArgs.IoArgsEventProcesser {

    private Receiver receiver;
    private AtomicBoolean isClosed = new AtomicBoolean(false);
    private ReceivePacketCallback callback;
    private IoArgs ioArgs = new IoArgs();
    private ReceivePacket<?> packetTemp;
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
            complete(false);
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

    private void assemblePacket(IoArgs args) {
        if (packetTemp == null) {
            int length = args.readLength();
            packetTemp = new StringReceivePacket(length);
            packetChannel = Channels.newChannel(packetTemp.open());
            total = length;
            position = 0;
        } else {
            try {
                int count = args.writeTo(packetChannel);
                position += count;
                if (position == total) {
                    complete(true);
                }
            } catch (IOException e) {
                complete(false);
            }
        }
    }

    private void complete(boolean isSucceed) {
        ReceivePacket packet = packetTemp;
        CloseUtils.close(packet, packetChannel);
        packetTemp = null;
        packetChannel = null;
        if (packet != null) {
            callback.onReceivePacketCompleted(packet);
        }
    }

    @Override
    public IoArgs provideIoArgs() {
        IoArgs args = ioArgs;
        int size;
        if (packetTemp == null) {
            size = 4;
        } else {
            size = (int) Math.min(total - position, args.capacity());
        }
        args.limit(size);
        return args;
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        assemblePacket(args);
        registerReceive();
    }

    @Override
    public void onConsumeFailed(IoArgs args, IOException ex) {
        ex.printStackTrace();
    }
}
