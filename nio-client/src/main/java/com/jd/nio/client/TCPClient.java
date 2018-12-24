package com.jd.nio.client;

import com.jd.bean.ServerInfo;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class TCPClient {

    private ClientHandler clientHandler;

    public TCPClient(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    public void send(String msg) {
        clientHandler.send(msg);
    }

    public void exit() {
        this.clientHandler.exitBySelf();
    }

    public static TCPClient startWith(ServerInfo info) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.socket().connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()), 3000);

        System.out.println("已发起服务器连接，并进入后续流程～");
        System.out.println("客户端信息：" + channel.getLocalAddress().toString());
        System.out.println("服务器信息：" + channel.getRemoteAddress().toString());

        ClientHandler clientHandler = new ClientHandler(channel, new ClientHandlerCallback() {
            @Override
            public void onSelfClosed(ClientHandler handler) {
                System.out.println("客户端已关闭");
            }

            @Override
            public void onNewMessageArrived(ClientHandler handler, String msg) {
                System.out.println(msg);
            }
        });

        return new TCPClient(clientHandler);
    }
}
