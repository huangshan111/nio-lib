package com.jd.handler;

import com.jd.ClientListener;
import com.jd.CloseUtils;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by huangshan11 on 2018/12/12.
 */
public class ClientHandler extends Thread implements SendOthers {

    private Socket client;
    private boolean done = false;
    private ClientReadHandler readHandler;
    private ClientWriteHandler writeHandler;
    private CloseEvent closeEvent;
    private ClientListener clientListener;

    public ClientHandler(ClientListener clientListener, Socket socket, CloseEvent closeEvent) throws IOException {
        this.client = socket;
        this.readHandler = new ClientReadHandler(socket.getInputStream(), this);
        this.writeHandler = new ClientWriteHandler(socket.getOutputStream());
        this.closeEvent = closeEvent;
        this.clientListener = clientListener;
    }

    public void exitBySelf() {
        exit();
        closeEvent.closeNotify(this);
    }

    public void exit() {
        readHandler.exit();
        writeHandler.exit();
        done = true;
        CloseUtils.close(client);
    }

    public void send(String msg) {
        writeHandler.send(msg);
    }

    public boolean isDone() {
        return done;
    }

    @Override
    public void run() {
        super.run();
        System.out.println("新客户端连接：" + client.getInetAddress() +
                " P:" + client.getPort());
        readHandler.start();
    }

    public void broadOthers(String msg) {
        clientListener.sendOthers(msg, this);
    }
}
