package com.jd.nio;

import java.nio.channels.SocketChannel;

/**
 * Created by huangshan11 on 2018/12/17.
 */
public interface OnChannelStatusChangedListener {
    void onChannelClosed(SocketChannel channel);
}
