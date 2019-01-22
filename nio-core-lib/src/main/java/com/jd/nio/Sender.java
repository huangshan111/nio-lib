package com.jd.nio;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by huangshan11 on 2018/12/17.
 */
public interface Sender extends Closeable {

    boolean postSendAsync() throws IOException;

    void setSendEventListener(IoArgs.IoArgsProcesser processer);
}
