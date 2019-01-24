package com.jd.nio;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Created by huangshan11 on 2018/12/17.
 */
public class IoArgs {

    private ByteBuffer byteBuffer = ByteBuffer.allocate(5);
    private int limit = 5;

    /**
     * 把文件的数据发送给对方，需要先将文件转换为一个ReadableByteChannel，可读的Channel
     * 然后从这个Channel读取数据到byteBuffer里面，也就是写入到byteBuffer，
     * 所以这个操作本质是写byteBuffer write to byteBuffer
     */
    public int readFrom(ReadableByteChannel channel) throws IOException {
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

    /**
     * 从对方接收数据，所以现在byteBuffer里有数据，你要把byteBuffer的数据写入到一个Channel
     * 这个Channel是一个可写的WritableByteChannel，WritableByteChannel
     * 本质最终可以理解为一个FileOutputStream，最终写到了文件
     */
    public int writeTo(WritableByteChannel channel) throws IOException {
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
        startWriting();
        byteBuffer.putInt(len);
        finishWriting();
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

    public interface IoArgsEventProcesser {

        IoArgs provideIoArgs();

        void onConsumeCompleted(IoArgs args);

        void onConsumeFailed(IoArgs args, IOException ex);
    }
}
