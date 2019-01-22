package com.jd.nio;

import java.io.InputStream;

/**
 * Created by huangshan11 on 2018/12/19.
 */
public abstract class SendPacket<T extends InputStream> extends Packet<T> {
    private boolean isCanceled;

    public boolean isCanceled() {
        return isCanceled;
    }
}
