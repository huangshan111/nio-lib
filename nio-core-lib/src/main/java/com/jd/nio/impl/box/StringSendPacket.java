package com.jd.nio.impl.box;

import com.jd.nio.SendPacket;

import java.io.ByteArrayInputStream;

/**
 * Created by huangshan11 on 2018/12/19.
 */
public class StringSendPacket extends SendPacket<ByteArrayInputStream> {

    private final byte[] bytes;

    public StringSendPacket(String msg) {
        this.bytes = msg.getBytes();
        length = this.bytes.length;
    }

    @Override
    protected ByteArrayInputStream createStream() {
        return new ByteArrayInputStream(bytes);
    }
}
