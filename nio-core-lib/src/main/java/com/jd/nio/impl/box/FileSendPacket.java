package com.jd.nio.impl.box;

import com.jd.nio.SendPacket;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by huangshan11 on 2018/12/19.
 */
public class FileSendPacket extends SendPacket<FileInputStream> {

    private final File file;

    public FileSendPacket(File file) {
        this.file = file;
        length = file.length();
    }

    @Override
    protected FileInputStream createStream() {
        return null;
    }
}
