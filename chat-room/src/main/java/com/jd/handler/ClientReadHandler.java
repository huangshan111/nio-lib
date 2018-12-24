package com.jd.handler;

import com.jd.CloseUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by huangshan11 on 2018/12/12.
 */
public class ClientReadHandler extends Thread {

    private boolean done = false;
    private final InputStream inputStream;
    private ClientHandler clientHandler;

    public ClientReadHandler(InputStream inputStream, ClientHandler clientHandler) {
        this.inputStream = inputStream;
        this.clientHandler = clientHandler;
    }

    public void exit() {
        done = true;
        CloseUtils.close(inputStream);
    }

    @Override
    public void run() {
        super.run();
        try {
            // 得到输入流，用于接收数据
            BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));
            do {
                String str = socketInput.readLine();
                System.out.println("转发消息:" + str);
                clientHandler.broadOthers(str);
            } while (!done);
        } catch (IOException e) {
            if (!done) {
                System.out.println("连接异常断开");
                clientHandler.exitBySelf();
            }
        } finally {
            CloseUtils.close(inputStream);
        }
    }
}
