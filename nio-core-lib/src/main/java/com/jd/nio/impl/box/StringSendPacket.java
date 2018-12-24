package com.jd.nio.impl.box;

import com.jd.nio.SendPacket;

import java.io.IOException;

/**
 * Created by huangshan11 on 2018/12/19.
 */
public class StringSendPacket extends SendPacket {

    private byte[] buffer;

    public StringSendPacket(String msg) {
        buffer = msg.getBytes();
        length = buffer.length;
    }

    @Override
    public byte[] bytes() {
        return buffer;
    }

    @Override
    public void close() throws IOException {

    }
}
