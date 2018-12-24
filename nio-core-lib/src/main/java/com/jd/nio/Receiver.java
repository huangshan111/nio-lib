package com.jd.nio;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by huangshan11 on 2018/12/17.
 */
public interface Receiver extends Closeable {

    boolean receiveAsync(IoArgs args) throws IOException;

    void setReceiveEventListener(IoArgs.IoArgsEventListener listener);
}
