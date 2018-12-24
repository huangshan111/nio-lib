package com.jd.server;

import com.jd.constants.TCPConstants;

import java.io.IOException;

/**
 * Created by huangshan11 on 2018/12/12.
 */
public class Server {
    public static void main(String[] args) {

        ServerProvider.start(TCPConstants.PORT_SERVER);

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ServerProvider.stop();
    }
}
