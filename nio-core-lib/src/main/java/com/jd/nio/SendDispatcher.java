package com.jd.nio;

import java.io.Closeable;

/**
 * Created by huangshan11 on 2018/12/19.
 */
public interface SendDispatcher extends Closeable {

    void send(SendPacket packet);

    void cancel(SendPacket packet);
}
