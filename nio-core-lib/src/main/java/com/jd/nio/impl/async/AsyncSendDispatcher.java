package com.jd.nio.impl.async;

import com.jd.nio.IoArgs;
import com.jd.nio.SendDispatcher;
import com.jd.nio.SendPacket;
import com.jd.nio.Sender;
import com.jd.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by huangshan11 on 2018/12/19.
 */
public class AsyncSendDispatcher implements SendDispatcher, IoArgs.IoArgsEventProcesser {

    private final Sender sender;
    private final Queue<SendPacket> queue = new ConcurrentLinkedQueue<>();
    private AtomicBoolean isSending = new AtomicBoolean(false);
    private AtomicBoolean isClosed = new AtomicBoolean(false);

    private IoArgs ioArgs = new IoArgs();
    private SendPacket<?> packetTemp;
    private ReadableByteChannel packetChannel;
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
        if (position >= total) {
            complete(position == total);
            sendNextPacket();
            return;
        }
        try {
            sender.postSendAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            isSending.set(false);
            complete(false);
        }
    }

    private void complete(boolean isSuccess) {
        SendPacket packet = packetTemp;
        if (packet != null) {
            CloseUtils.close(packet);
            packetTemp = null;
        }

        if (packetChannel != null) {
            CloseUtils.close(packetChannel);
            packetChannel = null;
        }

        position = 0;
        total = 0;
    }

    @Override
    public IoArgs provideIoArgs() {
        IoArgs args = ioArgs;
        if (packetChannel == null) {
            packetChannel = Channels.newChannel(packetTemp.open());
            args.limit(4);
            args.writeLength((int) packetTemp.length());
        } else {
            int limit = (int) Math.min(args.capacity(), total - position);
            args.limit(limit);
            try {
                int count = args.readFrom(packetChannel);
                position += count;
            } catch (IOException e) {
                return null;
            }
        }
        return args;
    }

    @Override
    public void onConsumeCompleted(IoArgs args) {
        sendCurrentPacket();
    }

    @Override
    public void onConsumeFailed(IoArgs args, IOException ex) {
        ex.printStackTrace();
    }
}
