package com.jd.nio;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by huangshan11 on 2018/12/17.
 */
public interface Receiver extends Closeable {

    boolean postReceiveAsync() throws IOException;

    void setReceiveEventListener(IoArgs.IoArgsEventProcesser processer);
}
