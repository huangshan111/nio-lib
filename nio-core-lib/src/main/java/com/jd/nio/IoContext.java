package com.jd.nio;

import java.io.IOException;

/**
 * Created by huangshan11 on 2018/12/17.
 */
public class IoContext {

    private static IoSelectorProvider instance;

    public static IoSelectorProvider get() {
        if (instance == null) {
            try {
                instance = new IoSelectorProvider();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }
}
