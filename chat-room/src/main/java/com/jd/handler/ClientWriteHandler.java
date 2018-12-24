package com.jd.handler;

import com.jd.CloseUtils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by huangshan11 on 2018/12/12.
 */
public class ClientWriteHandler {

    private ExecutorService executorService;
    private PrintStream ps;
    private boolean done = false;

    public ClientWriteHandler(OutputStream outputStream) {
        ps = new PrintStream(outputStream);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void exit() {
        done = true;
        CloseUtils.close(ps);
        executorService.shutdownNow();
    }

    public void send(final String msg) {
        if (!done) {
            executorService.submit(new Runnable() {
                public void run() {
                    ps.println(msg);
                }
            });
        }
    }
}
