package com.lody.welike.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 高效的IO流的操纵类,
 * 使用了{@link PoolingByteArrayOutputStream}替代ByteArrayOutputStream以提高性能.
 *
 * @author Lody
 */
public class IOUtils {

    /**
     * 将一个输入流转换为一个byte[]数组.
     *
     * @param input
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(InputStream input)
            throws IOException {
        ByteArrayOutputStream output = new PoolingByteArrayOutputStream(ByteArrayPool.get());
        copy(input, output);
        return output.toByteArray();
    }

    /**
     * 将一个输入流转换为一个byte[]数组.
     *
     * @param input
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(InputStream input, int len)
            throws IOException {

        ByteArrayOutputStream output = new PoolingByteArrayOutputStream(ByteArrayPool.get(), len);
        copy(input, output);
        return output.toByteArray();
    }

    /**
     * 将一个输入流拷贝到一个输出流
     *
     * @param input
     * @param output
     * @return
     * @throws IOException
     */
    public static int copy(InputStream input, OutputStream output)
            throws IOException {
        long count = copyLarge(input, output);
        if (count > 2147483647L) {
            return -1;
        }
        return (int) count;
    }

    /**
     * 将一个输入流拷贝到输出流
     *
     * @param input
     * @param output
     * @return
     * @throws IOException
     */
    public static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = ByteArrayPool.get().getBuf(5096);
        long count = 0L;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }


    /**
     * 将文件转换为字节数组
     */
    public static byte[] getBytes(File f) throws Exception {
        FileInputStream in = new FileInputStream(f);
        ByteArrayOutputStream out = new PoolingByteArrayOutputStream(ByteArrayPool.get());
        byte[] b = ByteArrayPool.get().getBuf(1024);
        int n;
        while ((n = in.read(b)) != -1) {
            out.write(b, 0, n);
        }
        out.flush();
        in.close();
        try{
        	return out.toByteArray();
        }finally{
        	out.close();
        }
    }
}
