package com.jd.nio.impl.async;

import com.jd.nio.IoArgs;
import com.jd.nio.SendDispatcher;
import com.jd.nio.SendPacket;
import com.jd.nio.Sender;
import com.jd.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by huangshan11 on 2018/12/19.
 */
public class AsyncSendDispatcher implements SendDispatcher, IoArgs.IoArgsProcesser {

    private final Sender sender;
    private final Queue<SendPacket> queue = new ConcurrentLinkedQueue<>();
    private AtomicBoolean isSending = new AtomicBoolean(false);
    private AtomicBoolean isClosed = new AtomicBoolean(false);

    private IoArgs ioArgs = new IoArgs();
    private SendPacket packetTemp;
    private WritableByteChannel packetChannel;
    private long total;
    private long position;

    public AsyncSendDispatcher(Sender sender) {
        sender.setSendEventListener(this);
        this.sender = sender;
    }

    @Override
    public void send(SendPacket packet) {
        queue.offer(packet);
        if (isSending.compareAndSet(false, true)) {
            sendNextPacket();
        }
    }

    @Override
    public void cancel(SendPacket packet) {

    }

    private SendPacket takePacket() {
        SendPacket packet = queue.poll();
        if (packet != null && packet.isCanceled()) {
            return takePacket();
        }
        return packet;
    }

    private void sendNextPacket() {
        SendPacket temp = this.packetTemp;
        if (temp != null) {
            CloseUtils.close(temp);
        }

        SendPacket packet = this.packetTemp = takePacket();
        if (packet == null) {
            isSending.set(false);
            return;
        }

        total = packet.length();
        position = 0;
        sendCurrentPacket();
    }

    private void sendCurrentPacket() {
        IoArgs args = ioArgs;
        args.startWriting();
        if (position >= total) {
            sendNextPacket();
            return;
        } else if (position == 0) {
            args.writeLength(total);
        }

        byte[] bytes = packetTemp.bytes();
        int count = args.readFrom(bytes, position);
        position += count;

        args.finishWriting();

        try {
            sender.sendAsync(args);
        } catch (IOException e) {
            closeAndNotify();
        }

    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            isSending.set(false);
            SendPacket packet = packetTemp;
            if (packet != null) {
                CloseUtils.close(packet);
                packetTemp = null;
            }
        }
    }

    @Override
    public IoArgs provideIoArgs() {
        IoArgs args = ioArgs;
        args.startWriting();
        if (position >= total) {
            sendNextPacket();
            return;
        } else if (position == 0) {
            args.writeLength(total);
        }

        byte[] bytes = packetTemp.bytes();
        int count = args.readFrom(bytes, position);
        position += count;
        return null;
    }

    @Override
    public void onConsumerSuccess(IoArgs args) {

    }

    @Override
    public void onConsumerFail(IoArgs args, IOException ex) {
        ex.printStackTrace();
    }
}
