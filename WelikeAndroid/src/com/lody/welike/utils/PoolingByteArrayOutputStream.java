package com.lody.welike.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 继承自ByteArrayOutputStream，
 * 自带的 ByteArrayOutputStream 中用于接受写入 bytes 的 buf，
 * 每次空间不足时便会 new 一个更大容量的 byte[]，
 * 而 PoolingByteArrayOutputStream 使用了 {@link ByteArrayPool} 作为 Byte[] 缓存来减少这种操作，从而提高性能。
 *
 * @author Lody
 * @version 1.0
 */
public class PoolingByteArrayOutputStream extends ByteArrayOutputStream {
    /**
     * 默认byte[]的大小
     */
    private static final int DEFAULT_SIZE = 256;

    /**
     * byte[]的缓冲池
     */
    private final ByteArrayPool mPool;

    /**
     * 带byte[]缓冲池的输出流，使用默认的byte[]长度
     */
    public PoolingByteArrayOutputStream(ByteArrayPool pool) {
        this(pool, DEFAULT_SIZE);
    }

    public PoolingByteArrayOutputStream(ByteArrayPool pool, int size) {
        mPool = pool;
        /**
         * buf,该输出流将要内容写入的目标byte[],如果多次写入，buf长度不够的时候需要扩展长度
         */
        buf = mPool.getBuf(Math.max(size, DEFAULT_SIZE));
    }

    @Override
    public void close() throws IOException {
        //当输出流关闭的时候，释放该byte[]回到byte缓冲池中
        mPool.returnBuf(buf);
        buf = null;
        super.close();
    }


    @Override
    public void finalize() {
        //当垃圾回收机制准备回收该输出流时，将该byte[]回收到缓冲池
        mPool.returnBuf(buf);
    }

    /**
     * 扩充当前输出流正在使用的byte[]的大小
     */
    private void expand(int i) {
        if (count + i <= buf.length) {
            //当已经写入的字节数加上预计扩展的字节数之和，仍然不大于当前的byte[]的长度时，不需要扩展
            return;
        }
        //当当前的byte[]不再满足时，需要从byte[]缓冲池中获取一个byte[]，大小为(count + i) * 2
        byte[] newBuf = mPool.getBuf((count + i) * 2);
        //将当前的byte[]内容复制到新的byte[]中
        System.arraycopy(buf, 0, newBuf, 0, count);
        //将旧的byte[]进行回收到byte[]缓冲池中
        mPool.returnBuf(buf);
        buf = newBuf;
    }

    /**
     * 从buffer的offset位置读len长度的内容，写入到输出流的byte[]（buf）中
     */
    @Override
    public synchronized void write(byte[] buffer, int offset, int len) {
        expand(len);
        super.write(buffer, offset, len);
    }

    @Override
    public synchronized void write(int oneByte) {
        //扩展1个字节长度
        expand(1);
        super.write(oneByte);
    }
}