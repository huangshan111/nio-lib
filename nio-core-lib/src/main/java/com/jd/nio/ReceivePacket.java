package com.jd.nio;

/**
 * Created by huangshan11 on 2018/12/19.
 */
public abstract class ReceivePacket extends Packet {
    public abstract void save(byte[] bytes, int count);
}
