package com.jd.server;

import com.jd.ByteUtils;
import com.jd.constants.UDPConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by huangshan11 on 2018/12/10.
 */
public class ServerProvider {

    private static Provider PROVIDER_INSTANCE;

    public static void start(int port) {
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(port, sn);
        provider.start();
        PROVIDER_INSTANCE = provider;
    }

    static void stop() {
        if (PROVIDER_INSTANCE != null) {
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }

    private static class Provider extends Thread {

        private final int port;
        private final byte[] sn;
        private DatagramSocket ds;
        private boolean done;
        private byte[] buffer = new byte[128];
        private int minLen = UDPConstants.HEADER.length + 2 + 4;

        Provider(int port, String sn) {
            this.port = port;
            this.sn = sn.getBytes();
        }

        void exit() {
            done = true;
            close();
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        @Override
        public void run() {
            System.out.println("Server UDPProvider started.");
            //作为接收者，指定一个端口接收
            try {
                ds = new DatagramSocket(UDPConstants.PORT_SERVER);
                DatagramPacket recPack = new DatagramPacket(buffer, buffer.length);
                while (!done) {
                    ds.receive(recPack);
                    String clientIp = recPack.getAddress().getHostAddress();
                    int clientPort = recPack.getPort();
                    int dataLen = recPack.getLength();
                    byte[] data = recPack.getData();

                    boolean isValid = dataLen >= minLen && ByteUtils.startsWith(data, UDPConstants.HEADER);
                    System.out.println("ServerProvider receive from ip:" + clientIp
                            + "\tport:" + clientPort + "\tdataValid:" + isValid);
                    if (!isValid) {
                        continue;
                    }

                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, UDPConstants.HEADER.length, dataLen);

                    final short cmd = byteBuffer.getShort();
                    final int responsePort = byteBuffer.getInt();
                    if (cmd != 1 || responsePort <= 0) {
                        System.out.println("ServerProvider receive cmd:" + cmd + "\tresponsePort:" + responsePort);
                        continue;
                    }
                    // 构建一份回送数据
                    byteBuffer = ByteBuffer.wrap(buffer);
                    byteBuffer.put(UDPConstants.HEADER);
                    byteBuffer.putShort((short) 2);
                    byteBuffer.putInt(port);
                    byteBuffer.put(sn);
                    DatagramPacket responsePacket = new DatagramPacket(buffer,
                            byteBuffer.position() + 1,
                            recPack.getAddress(),
                            responsePort);
                    ds.send(responsePacket);
                    System.out.println("ServerProvider response to:" + clientIp + "\tport:" + responsePort + "\tdataLen:" + (byteBuffer.position() + 1));
                }
            } catch (IOException e) {
            } finally {
                close();
            }
        }
    }
}
