package com.jd.client;

import com.jd.ByteUtils;
import com.jd.constants.UDPConstants;
import com.jd.server.ServerInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by huangshan11 on 2018/12/12.
 */
public class ClientSearcher {

    public static List<ServerInfo> searchServer(int timeout) {
        System.out.println("UDPSearcher Started.");

        CountDownLatch recviceLatch = new CountDownLatch(1);
        Listener listener = listen(recviceLatch);
        try {
            sendBroadcast();
            recviceLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listener.getServersAndClose();
    }

    private static void sendBroadcast() throws IOException {
        //搜索方，端口系统自动分配
        DatagramSocket ds = new DatagramSocket();
        ByteBuffer buffer = ByteBuffer.allocate(128);
        buffer.put(UDPConstants.HEADER);
        buffer.putShort((short) 1);
        buffer.putInt(UDPConstants.PORT_CLIENT_RESPONSE);
        DatagramPacket requestPack = new DatagramPacket(buffer.array(), buffer.position() + 1);
        requestPack.setAddress(InetAddress.getByName("255.255.255.255"));
        requestPack.setPort(UDPConstants.PORT_SERVER);
        ds.send(requestPack);
    }

    private static Listener listen(CountDownLatch recviceLatch) {
        CountDownLatch startedLatch = new CountDownLatch(1);
        Listener listener = new Listener(recviceLatch, startedLatch, UDPConstants.PORT_CLIENT_RESPONSE);
        listener.start();
        try {
            startedLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return listener;
    }

    private static class Listener extends Thread {

        private CountDownLatch recviceLatch;
        private CountDownLatch startedLatch;
        private final int listenPort;
        private boolean done;
        private DatagramSocket ds;
        private byte[] buffer = new byte[128];
        private int minLen = UDPConstants.HEADER.length + 2 + 4;
        private List<ServerInfo> serverInfoList = new ArrayList<ServerInfo>();

        public Listener(CountDownLatch recviceLatch, CountDownLatch startedLatch, int listenPort) {
            this.recviceLatch = recviceLatch;
            this.startedLatch = startedLatch;
            this.listenPort = listenPort;
        }

        public List<ServerInfo> getServersAndClose() {
            done = true;
            close();
            return serverInfoList;
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        @Override
        public void run() {
            startedLatch.countDown();

            try {
                ds = new DatagramSocket(listenPort);
                DatagramPacket recPack = new DatagramPacket(buffer, buffer.length);
                while (!done) {
                    ds.receive(recPack);

                    final String ip = recPack.getAddress().getHostAddress();
                    final int port = recPack.getPort();
                    final int dataLen = recPack.getLength();
                    final byte[] data = recPack.getData();

                    boolean isValid = dataLen >= minLen && ByteUtils.startsWith(data, UDPConstants.HEADER);
                    System.out.println("UDPSearcher receive form ip:" + ip
                            + "\tport:" + port + "\tdataValid:" + isValid);
                    if (!isValid) {
                        continue;
                    }

                    ByteBuffer byteBuffer = ByteBuffer.wrap(data, UDPConstants.HEADER.length, dataLen);
                    final short cmd = byteBuffer.getShort();
                    final int serverPort = byteBuffer.getInt();
                    if (cmd != 2 || serverPort <= 0)
                        continue;
                    final String sn = new String(buffer, minLen, dataLen - minLen);
                    ServerInfo serverInfo = new ServerInfo(serverPort, ip, sn);
                    System.out.println("Search a server ip:" + ip
                            + "\tport:" + serverPort + "\tsn:" + sn);
                    serverInfoList.add(serverInfo);
                    recviceLatch.countDown();
                }
            } catch (IOException e) {
            } finally {
                close();
            }
        }
    }
}
