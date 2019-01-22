package com.jd.nio;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by huangshan11 on 2018/12/19.
 */
public abstract class Packet<T extends Closeable> implements Closeable {
    protected byte type;
    protected long length;

    private T stream;

    public byte type() {
        return type;
    }

    public long length() {
        return length;
    }

    protected abstract T createStream();

    protected void closeStream(T stream) {

    }

    public final T open() {
        if (stream == null) {
            stream = createStream();
        }
        return stream;
    }

    @Override
    public final void close() throws IOException {
        if (stream != null) {
            stream.close();
            closeStream(stream);
            stream = null;
        }
    }
}
