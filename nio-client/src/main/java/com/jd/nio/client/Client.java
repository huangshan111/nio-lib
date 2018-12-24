package com.jd.nio.client;


import com.jd.bean.ServerInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by huangshan11 on 2018/12/12.
 */
public class Client {
    public static void main(String[] args) throws IOException {
        ServerInfo info = ClientSearcher.searchServer(10000);
        System.out.println("Server:" + info);
        if (info == null) {
            return;
        }

        try {
            TCPClient tcpClient = TCPClient.startWith(info);
            if (tcpClient == null) {
                System.out.println("连接异常");
            } else {
                System.out.println("连接成功");
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String input = br.readLine();
                if (input.equals("bye"))
                    break;
                tcpClient.send(input);
            }
            tcpClient.exit();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("连接异常");
        }
    }
}
