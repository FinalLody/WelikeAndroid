package com.lody.welike.bitmap;

import android.graphics.Bitmap;

import com.lody.welike.utils.MemoryLruCache;

/**
 * 用于图片加载的LruCache.
 *
 * @author Lody
 * @version 1.0
 */
public class ImageMemoryLruCache extends MemoryLruCache<String, Bitmap> {

    /**
     * @param maxSize LruCache内存上限
     */
    public ImageMemoryLruCache(int maxSize) {
        super(maxSize);
    }

    /**
     * @param config Bitmap加载引擎的配置
     */
    public ImageMemoryLruCache(BitmapConfig config) {
        this(config.memoryCacheSize);
    }


    @Override
    protected int sizeOf(String key, Bitmap value) {
        //NOTE:有些人特意做了个判断,
        //在SDK_INT >= 12 时使用value.getByteCount(),
        //SDK_INT < 12 时使用value.getRowBytes() * value.getHeight(),
        //其实这不都一样么,value.getByteCount()内部还是调用了value.getRowBytes() * value.getHeight().

        return value.getRowBytes() * value.getHeight();
    }

    /**
     * 取得内存中的Bitmap
     *
     * @param url
     * @return
     */
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    /**
     * 将一张Bitmap放到LruCache
     *
     * @param url
     * @param bitmap
     */
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }

    /**
     * 清除全部缓存
     */
    public void clearAllBitmap() {
        getMap().clear();
    }

    /**
     * 移除一个张图片
     *
     * @param url
     * @return
     */
    public Bitmap removeBitmap(String url) {
        return remove(url);
    }


}
