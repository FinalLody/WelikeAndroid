package com.lody.welike.bitmap;

import com.lody.welike.WelikeHttp;
import com.lody.welike.http.HttpConfig;

/**
 * 图片加载引擎的配置信息
 *
 * @author Lody
 * @version 1.0
 */
public class BitmapConfig {

    /**
     * 图片磁盘缓存的文件夹名
     */
    public static String CACHE_DIR_NAME = "bitmap";

    /**
     * 是否开启磁盘缓存
     */
    public boolean enableDiskCache = true;

    /**
     * 内存缓存大小,默认为OOM上限的八分之一
     */
    public int memoryCacheSize = (int) (Runtime.getRuntime().maxMemory() / 8);

    /**
     * 磁盘缓存大小
     */
    public long diskCacheSize = HttpConfig.CACHE_MAX_SIZE;

    /**
     * 用于发起加载图片的Http请求
     */
    private WelikeHttp welikeHttp;

    /**
     * 是否开启Debug模式
     */
    public boolean debugMode = true;

    /**
     * 图片内存LruCache缓存池
     */
    private ImageMemoryLruCache memoryLruCache;


    public BitmapConfig() {
        HttpConfig config = new HttpConfig(CACHE_DIR_NAME, diskCacheSize);
        welikeHttp = new WelikeHttp(config);
        memoryLruCache = new ImageMemoryLruCache(this);
    }

    /**
     * 应用另一个配置
     *
     * @param config 配置
     */
    public void applyConfig(BitmapConfig config) {
        this.diskCacheSize = config.diskCacheSize;
        this.memoryCacheSize = config.memoryCacheSize;
        this.enableDiskCache = config.enableDiskCache;
        this.welikeHttp = config.welikeHttp;
        config.enableDiskCache = enableDiskCache;
    }

    /**
     * 取得用于发起加载图片的Http请求的WelikeHttp
     *
     * @return 用于发起加载图片的Http请求的WelikeHttp
     */
    public WelikeHttp getWelikeHttp() {
        return this.welikeHttp;
    }

    /**
     * @return 图片内存LruCache缓存池
     */
    public ImageMemoryLruCache getMemoryLruCache() {
        return memoryLruCache;
    }

}
