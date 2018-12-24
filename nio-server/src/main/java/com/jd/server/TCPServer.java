package com.jd.server;

import com.jd.CloseUtils;
import com.jd.handler.ClientHandler;
import com.jd.handler.ClientHandlerCallback;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by huangshan11 on 2018/12/12.
 */
public class TCPServer {

    private final int port;
    private ClientListener listener;
    private Selector selector;
    private ServerSocketChannel server;
    private List<ClientHandler> clientHandlerList;

    public TCPServer(int port) {
        this.port = port;
        this.clientHandlerList = new ArrayList<>();
    }

    public boolean start() {
        try {
            selector = Selector.open();
            ServerSocketChannel server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(port));

            server.register(selector, SelectionKey.OP_ACCEPT);
            this.server = server;

            System.out.println("服务器已经启动，服务器地址:" + server.getLocalAddress());
            this.listener = new ClientListener();
            this.listener.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        if (listener != null) {
            listener.exit();
        }
        for (ClientHandler clientHandler : clientHandlerList) {
            CloseUtils.close(clientHandler);
        }
        CloseUtils.close(server, selector);
    }

    public void sendAll(String msg) {
        listener.sendAll(msg);
    }

    class ClientListener extends Thread {

        private boolean done;

        public void exit() {
            done = true;
            selector.wakeup();
        }

        public void sendAll(String msg) {
            for (ClientHandler clientHandler : clientHandlerList) {
                clientHandler.send(msg);
            }
        }

        @Override
        public void run() {
            Selector selector = TCPServer.this.selector;
            try {
                while (true) {
                    if (selector.select() == 0) {
                        if (done) {
                            break;
                        }
                        continue;
                    }

                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            ClientHandler clientHandler = new ClientHandler(socketChannel, new ClientHandlerCallback() {
                                @Override
                                public void onSelfClosed(ClientHandler handler) {
                                    synchronized (TCPServer.this) {
                                        clientHandlerList.remove(handler);
                                    }
                                }

                                @Override
                                public void onNewMessageArrived(ClientHandler handler, String msg) {
                                    System.out.println(handler.getClientInfo() + "发来消息:" + msg);
                                    for (ClientHandler clientHandler : clientHandlerList) {
                                        if (clientHandler == handler)
                                            continue;
                                        clientHandler.send(msg);
                                    }
                                }
                            });

                            synchronized (TCPServer.this) {
                                clientHandlerList.add(clientHandler);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("服务器关闭了");
        }
    }
}
