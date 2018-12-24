package com.jd.server;

import com.jd.ClientListener;

import java.io.IOException;

/**
 * Created by huangshan11 on 2018/12/12.
 */
public class TCPServer {

    private final int port;
    private ClientListener clientListener;

    public TCPServer(int port) {
        this.port = port;
    }

    public boolean start() {
        try {
            ClientListener listener = new ClientListener(port);
            clientListener = listener;
            listener.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        if (clientListener != null) {
            clientListener.exit();
        }
    }

    public void sendAll(String msg) {
        clientListener.sendAll(msg);
    }
}
