package com.jd.nio.impl.box;

import com.jd.nio.ReceivePacket;

import java.io.ByteArrayOutputStream;

/**
 * Created by huangshan11 on 2018/12/19.
 */
public class StringReceivePacket extends ReceivePacket<ByteArrayOutputStream> {

    private String string;

    public StringReceivePacket(int len) {
        length = len;
    }

    public String string() {
        return string;
    }

    @Override
    protected void closeStream(ByteArrayOutputStream stream) {
        super.closeStream(stream);
        string = new String(stream.toByteArray());
    }

    @Override
    protected ByteArrayOutputStream createStream() {
        return new ByteArrayOutputStream((int) length);
    }
}
