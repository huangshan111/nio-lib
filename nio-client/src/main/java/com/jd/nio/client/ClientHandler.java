package com.jd.nio.client;

import com.jd.nio.Connector;
import com.jd.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by huangshan11 on 2018/12/12.
 */
public class ClientHandler extends Connector {

    private ClientHandlerCallback clientHandlerCallback;
    private String clientInfo;

    public ClientHandler(SocketChannel channel, ClientHandlerCallback clientHandlerCallback) throws IOException {
        this.clientInfo = channel.getRemoteAddress().toString();
        this.clientHandlerCallback = clientHandlerCallback;
        setup(channel);
    }

    public void exitBySelf() {
        exit();
        clientHandlerCallback.onSelfClosed(this);
    }

    private void exit() {
        CloseUtils.close(this);
        System.out.println(clientInfo + " 客户端已退出");
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        exitBySelf();
    }

    @Override
    protected void onReceiveNewMessage(String msg) {
        super.onReceiveNewMessage(msg);
        clientHandlerCallback.onNewMessageArrived(this, msg);
    }
}
