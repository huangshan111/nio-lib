package com.jd.nio;

import com.jd.nio.impl.async.AsyncReceiveDispatcher;
import com.jd.nio.impl.async.AsyncSendDispatcher;
import com.jd.nio.impl.box.StringReceivePacket;
import com.jd.nio.impl.box.StringSendPacket;
import com.jd.utils.CloseUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 * Created by huangshan11 on 2018/12/17.
 */
public class Connector implements OnChannelStatusChangedListener, Closeable {

    private String key = UUID.randomUUID().toString();
    private SocketChannel channel;
    private Sender sender;
    private Receiver receiver;
    private SendDispatcher sendDispatcher;
    private ReceiveDispatcher receiveDispatcher;

    public void setup(SocketChannel channel) {
        this.channel = channel;
        try {
            SocketChannelAdapter adapter = new SocketChannelAdapter(channel, IoContext.get(), this);
            this.sender = adapter;
            this.receiver = adapter;
            this.sendDispatcher = new AsyncSendDispatcher(sender);
            this.receiveDispatcher = new AsyncReceiveDispatcher(receiver, callback);
            this.receiveDispatcher.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {

    }

    public void send(String msg) {
        StringSendPacket sendPacket = new StringSendPacket(msg);
        sendDispatcher.send(sendPacket);
    }

    @Override
    public void close() throws IOException {
        this.sendDispatcher.close();
        this.receiveDispatcher.close();
        this.sender.close();
        this.receiver.close();
        CloseUtils.close(channel);
    }

    protected void onReceiveNewMessage(String msg) {

    }

    private ReceiveDispatcher.ReceivePacketCallback callback = new ReceiveDispatcher.ReceivePacketCallback() {
        @Override
        public void onReceivePacketCompleted(ReceivePacket packet) {
            if (packet instanceof StringReceivePacket) {
                StringReceivePacket stringReceivePacket = (StringReceivePacket) packet;
                onReceiveNewMessage(stringReceivePacket.string());
            }
        }
    };
}
