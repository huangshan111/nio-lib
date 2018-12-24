package com.jd.client;

import com.jd.server.ServerInfo;

import java.io.IOException;
import java.util.List;

/**
 * Created by huangshan11 on 2018/12/12.
 */
public class Client {
    public static void main(String[] args) {
        List<ServerInfo> serverInfoList = ClientSearcher.searchServer(10000);
        for (ServerInfo serverInfo : serverInfoList) {
            try {
                TCPClient.linkWith(serverInfo);
            } catch (IOException e) {
                System.out.println("连接服务器失败");
            }
        }
    }
}
