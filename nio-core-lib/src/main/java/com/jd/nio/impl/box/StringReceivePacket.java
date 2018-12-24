package com.jd.nio.impl.box;

import com.jd.nio.ReceivePacket;

import java.io.IOException;

/**
 * Created by huangshan11 on 2018/12/19.
 */
public class StringReceivePacket extends ReceivePacket {

    private int positioin;
    private byte[] buffer;

    public StringReceivePacket(int len) {
        buffer = new byte[len];
        length = len;
    }

    @Override
    public void save(byte[] bytes, int count) {
        System.arraycopy(bytes, 0, buffer, positioin, count);
        positioin += count;
    }

    @Override
    public void close() throws IOException {

    }

    public String string() {
        return new String(buffer);
    }
}
