package com.jd.handler;

import com.jd.CloseUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by huangshan11 on 2018/12/13.
 */
public class ReadHandler extends Thread {

    private final InputStream inputStream;
    private boolean done;

    public ReadHandler(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void exit() {
        done = true;
        CloseUtils.close(inputStream);
    }

    @Override
    public void run() {
        super.run();
        System.out.println("读取数据线程启动");
        try {
            // 得到输入流，用于接收数据
          //  BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

            do {
                byte[] buffer = new byte[128];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    String str = new String(buffer, 0, len);
                    System.out.println(str);
                }
            } while (!done);
        } catch (IOException e) {
            if (!done) {
                System.out.println("连接异常断开");
            }
        } finally {
            CloseUtils.close(inputStream);
        }
    }
}