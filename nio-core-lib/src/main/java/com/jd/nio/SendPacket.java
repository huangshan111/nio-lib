package com.jd.nio;

/**
 * Created by huangshan11 on 2018/12/19.
 */
public abstract class SendPacket extends Packet {
    private boolean isCanceled;

    public abstract byte[] bytes();

    public boolean isCanceled() {
        return isCanceled;
    }
}
