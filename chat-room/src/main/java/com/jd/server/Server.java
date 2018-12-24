package com.jd.server;

import com.jd.constants.TCPConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by huangshan11 on 2018/12/12.
 */
public class Server {
    public static void main(String[] args) {

        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("Start TCP server failed!");
            return;
        }

        ServerProvider.start(TCPConstants.PORT_SERVER);

        boolean done = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (!done) {
            try {
                String str = reader.readLine();
                if (str.equalsIgnoreCase("bye")) {
                    done = true;
                }
                tcpServer.sendAll(str);
            } catch (IOException e) {
                done = true;
            }
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ServerProvider.stop();
        tcpServer.stop();
    }
}
