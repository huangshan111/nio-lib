package com.jd.server;

import com.jd.CloseUtils;
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

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String str = null;
            do {
                str = reader.readLine();
                tcpServer.sendAll(str);
            } while (!"bye".equalsIgnoreCase(str));
        } catch (IOException e) {
            System.out.println("从键盘读取数据失败");
        }
        CloseUtils.close(reader);
        ServerProvider.stop();
        tcpServer.stop();
    }
}
