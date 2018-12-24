package com.jd;

import com.jd.handler.ClientHandler;
import com.jd.handler.CloseEvent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangshan11 on 2018/12/12.
 */
public class ClientListener extends Thread {

    private boolean done;

    private ServerSocket server;

    private List<ClientHandler> clientHandlerList;

    public ClientListener(int port) throws IOException {
        this.server = new ServerSocket(port);
        this.clientHandlerList = new ArrayList();
    }

    public void exit() {
        done = true;
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.exit();
        }
        close();
    }

    public void sendAll(String msg) {
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(msg);
        }
    }

    public void sendOthers(String msg, ClientHandler me) {
        for (ClientHandler clientHandler : clientHandlerList) {
            if (clientHandler == me)
                continue;
            clientHandler.send(msg);
        }
    }

    private void close() {
        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        System.out.println("server started, wait client connect~~~~");
        do {
            Socket socket;
            try {
                socket = server.accept();
            } catch (IOException e) {
                continue;
            }
            try {
                ClientHandler clientHandler = new ClientHandler(this, socket, new CloseEvent() {
                    public void closeNotify(ClientHandler handler) {
                        clientHandlerList.remove(handler);
                    }
                });
                clientHandler.start();
                clientHandlerList.add(clientHandler);
            } catch (IOException e) {
                System.out.println("客户端连接失败" + e.getMessage());
            }
        } while (!done);
        System.out.println("server shutdown");
    }
}