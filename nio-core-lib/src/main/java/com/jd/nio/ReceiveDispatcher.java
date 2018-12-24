package com.jd.nio;

import java.io.Closeable;

/**
 * Created by huangshan11 on 2018/12/19.
 */
public interface ReceiveDispatcher extends Closeable {
    void start();

    void stop();

    interface ReceivePacketCallback {
        void onReceivePacketCompleted(ReceivePacket packet);
    }
}
