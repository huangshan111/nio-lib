package com.jd.nio;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by huangshan11 on 2018/12/17.
 */
public class IoArgs {

    private ByteBuffer byteBuffer = ByteBuffer.allocate(8);
    private int limit = 8;

    //limit - position
    public int readFrom(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, byteBuffer.remaining());
        byteBuffer.put(bytes, offset, size);
        return size;
    }

    public int writeTo(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, byteBuffer.remaining());
        byteBuffer.get(bytes, offset, size);
        return size;
    }

    public int readFrom(SocketChannel channel) throws IOException {
        startWriting();
        int byteProduced = 0;
        while (byteBuffer.hasRemaining()) {
            int len = channel.read(byteBuffer);
            if (len < 0) {
                throw new EOFException();
            }
            byteProduced += len;
        }
        finishWriting();
        return byteProduced;
    }

    public int writeTo(SocketChannel channel) throws IOException {
        int byteProduced = 0;
        while (byteBuffer.hasRemaining()) {
            int len = channel.write(byteBuffer);
            if (len < 0) {
                throw new EOFException();
            }
            byteProduced += len;
        }
        return byteProduced;
    }

    public void startWriting() {
        byteBuffer.clear();
        byteBuffer.limit(limit);
    }

    public void finishWriting() {
        byteBuffer.flip();
    }

    public void writeLength(int len) {
        byteBuffer.putInt(len);
    }

    public int readLength() {
        return byteBuffer.getInt();
    }

    public void limit(int limit) {
        this.limit = limit;
    }

    public int capacity() {
        return byteBuffer.capacity();
    }

    public interface IoArgsEventListener {
        void onStarted(IoArgs args);

        void onCompleted(IoArgs args);
    }
}
