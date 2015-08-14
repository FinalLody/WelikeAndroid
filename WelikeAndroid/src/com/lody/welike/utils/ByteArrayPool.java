package com.lody.welike.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * 字节数组缓冲池:
 * <p/>
 * byte[] 的回收池，用于 byte[] 的回收再利用，减少了内存的分配和回收。
 * 主要通过一个元素长度从小到大排序的ArrayList作为 byte[] 的缓存，
 * 另有一个按使用时间先后排序的ArrayList属性用于缓存满时清理元素。
 *
 * @author Lody
 * @version 1.0
 */
public class ByteArrayPool {


    /**
     * ByteArrayPool的单例.
     */
    private static ByteArrayPool sPool = new ByteArrayPool(4096);

    /**
     * @return ByteArrayPool的单例
     */
    public static ByteArrayPool get() {
        return sPool;
    }

    /**
     * 按使用的先后时间顺序排序
     */
    private List<byte[]> mBuffersByLastUse = new LinkedList<>();
    /**
     * 按大小顺序排序
     */
    private List<byte[]> mBuffersBySize = new ArrayList<>(64);

    /**
     * 池中所有byte[]的长度之和
     */
    private int mCurrentSize = 0;

    /**
     * 池中单个byte[]的最大长度
     */
    private final int mSizeLimit;

    /**
     * 比较器，用于排序，按byte[]的字节长度进行排序
     */
    protected static final Comparator<byte[]> BUF_COMPARATOR = new Comparator<byte[]>() {
        @Override
        public int compare(byte[] lhs, byte[] rhs) {
            return lhs.length - rhs.length;
        }
    };

    /**
     * 创建byte[]缓冲池，并设定池中单个byte[]的最大长度
     *
     * @param sizeLimit 缓存池上限(字节为单位)
     */
    public ByteArrayPool(int sizeLimit) {
        mSizeLimit = sizeLimit;
    }

    /**
     * 从池中获取一个可用的byte[]，如果没有，就创建一个。参数为想要获取多大长度的byte[].
     *
     * @param len 需要的字节数组的最小长度,取得的数组往往长度大于这个值
     * @return 池中的或新建的数组
     */
    public synchronized byte[] getBuf(int len) {
        //遍历按长度排序的池
        for (int i = 0; i < mBuffersBySize.size(); i++) {
            byte[] buf = mBuffersBySize.get(i);
            //如果当前的byte[]的长度大于给定的长度，就返回该byte[]
            if (buf.length >= len) {
                //池中所有byte[]的长度之和减去返回出去的byte[]的长度
                mCurrentSize -= buf.length;
                //从按顺序排列的池中移除该byte[]
                mBuffersBySize.remove(i);
                //从按使用顺序排列的池中移除该byte[]表示该byte[]正在使用中，其他不能再使用该byte[]
                mBuffersByLastUse.remove(buf);
                return buf;
            }
        }
        //创建一个新的byte[]
        return new byte[len];
    }

    /**
     * 当使用完一个byte[]后，将该byte[]返回到池中
     *
     * @param buf 返回池中的数组
     */
    public synchronized void returnBuf(byte[] buf) {
        //如果为空或者超过了设定的单个byte[]的最大长度  那么就不再池中保存该byte[]
        if (buf == null || buf.length > mSizeLimit) {
            return;
        }
        //在按使用时间顺序排序的池中添加该byte[]
        mBuffersByLastUse.add(buf);
        //利用二分查找法，找出在按大小排序的池中该byte[]应该存放的位置
        int pos = Collections.binarySearch(mBuffersBySize, buf, BUF_COMPARATOR);
        //如果找不到，则返回-1
        if (pos < 0) {
            //当找不到时，也就是在0位置添加
            pos = -pos - 1;
        }
        mBuffersBySize.add(pos, buf);
        //增加最大长度
        mCurrentSize += buf.length;
        //清理，不能超过字节总数最大值
        trim();
    }

    /**
     * 回收字节数组池
     */
    private synchronized void trim() {
        //当现有字节总数超过了设定的界限，那么需要清理
        while (mCurrentSize > mSizeLimit) {
            //按照使用的先后顺序来倾全力，最新使用的最先被清除
            byte[] buf = mBuffersByLastUse.remove(0);
            //同样在该池中也清除
            mBuffersBySize.remove(buf);
            //减小现有字节最大长度
            mCurrentSize -= buf.length;
        }
    }

}