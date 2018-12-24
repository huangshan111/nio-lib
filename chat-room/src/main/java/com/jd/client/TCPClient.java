package com.jd.client;

import com.jd.CloseUtils;
import com.jd.handler.ReadHandler;
import com.jd.server.ServerInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by huangshan11 on 2018/12/12.
 */
public class TCPClient {

    public static void linkWith(ServerInfo info) throws IOException {
        Socket socket = new Socket();

//        socket.setSoTimeout(3000);

        // 连接本地，端口2000；超时时间3000ms
        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()), 3000);

        try {
            todo(socket);
        } catch (IOException e) {
            System.out.println("异常关闭");
        }

        socket.close();
    }

    private static void todo(Socket socket) throws IOException {

        ReadHandler readHandler = new ReadHandler(socket.getInputStream());
        readHandler.start();

        //键盘输入流
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        //发送给服务器的打印流
        PrintStream ps = new PrintStream(socket.getOutputStream());

        while (true) {
            String str = input.readLine();
            ps.println(str);
            if (str.equalsIgnoreCase("bye")) {
                break;
            }
        }

        readHandler.exit();
        CloseUtils.close(ps, input, socket);
        System.out.println("客户端退出了");
    }
}
