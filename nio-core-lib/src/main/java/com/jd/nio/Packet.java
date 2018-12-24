package com.jd.nio;

import java.io.Closeable;

/**
 * Created by huangshan11 on 2018/12/19.
 */
public abstract class Packet implements Closeable {
    protected byte type;
    protected int length;

    public byte type() {
        return type;
    }

    public int length() {
        return length;
    }
}
