package com.lody.welike.http;

import com.lody.welike.WelikeContext;
import com.lody.welike.utils.AppUtils;
import com.lody.welike.utils.DiskLruCache;
import com.lody.welike.utils.WeLog;

import java.io.File;
import java.io.IOException;

/**
 * Http核心配置器,请根据需要配置本类.
 * 默认编码为UTF-8.
 *
 * @author Lody
 * @version 1.3
 */
public final class HttpConfig {

    /**
     * 默认缓存文件夹名
     */
    public static String DEFAULT_CACHE_DIR_NAME = "http";
    /**
     * 默认编码
     */
    public static String DEFAULT_HTTP_ENCODE = "UTF-8";

    /**
     * 默认缓存过期时间,默认为8分钟
     */
    public static long DEFAULT_EXPIRY_TIME = 8 * 1000 * 60;


    /**
     * 默认缓存上限
     */
    public static long CACHE_MAX_SIZE = 8 * 1024 * 1024 * 8;

    /**
     * 缓存上限,默认大小为{@value HttpConfig#CACHE_MAX_SIZE}.
     */
    public long maxDiskCacheSize = HttpConfig.CACHE_MAX_SIZE;

    /**
     * 缓存的文件夹名.
     */
    public String cacheDirName;

    /**
     * DiskLruCache缓存
     * 每一个Key对应两个值,
     * 第一个值是Http响应的缓存数据,
     * 第二个值是缓存的到期时间.
     */
    private DiskLruCache diskLruCache;

    /**
     * 编码类型,默认UTF-8
     */
    private String encodeType = DEFAULT_HTTP_ENCODE;

    /**
     * 开启磁盘缓存吗?
     */
    public boolean enableDiskCache = true;

    /**
     * 缓存有效时间,(默认的缓存时间为8分钟,如果想要让缓存永久有效,请设为0).
     */
    public long expiryDate = DEFAULT_EXPIRY_TIME;

    /**
     * 是否是调试模式(框架将在运行时输出调试信息)
     */
    public boolean debugMode = true;

    /**
     * 是否允许重定向?
     */
    private boolean allowUserInteraction = false;

    /**
     * Http请求的并发量
     */
    public int concurrency = 5;

    /**
     * Http配置创建工厂
     */
    private static HttpConfigFactory DEFAULT_FACTORY = new HttpConfigFactory.DefaultHttpConfigFactory();


    /**
     * @return 编码类型
     */
    public String getEncoding() {
        return encodeType;
    }

    /**
     * 设置编码类型
     *
     * @param encodeType 编码
     */
    public void setEncodeing(String encodeType) {
        this.encodeType = encodeType;
    }

    /**
     * 应用为另一个配置
     *
     * @param config 另一个配置
     */
    public void applyConfig(HttpConfig config) {
        this.debugMode = config.debugMode;
        this.encodeType = config.encodeType;
    }

    /**
     * 返回一个默认配置
     *
     * @return 创建的默认配置
     */
    public static HttpConfig newDefaultConfig() {
        return DEFAULT_FACTORY.newDefaultConfig();
    }

    public static void setDefaultHttpConfigFactory(HttpConfigFactory defaultHttpConfigFactory){
        HttpConfig.DEFAULT_FACTORY = defaultHttpConfigFactory;
    }

    /**
     * @return 是否允许重定向
     */
    public boolean isAllowUserInteraction() {
        return allowUserInteraction;
    }

    /**
     * 设置是否允许重定向
     *
     * @param allowUserInteraction
     */
    public void setAllowUserInteraction(boolean allowUserInteraction) {
        this.allowUserInteraction = allowUserInteraction;
    }

    /**
     * 取得DiskLruCache,如果创建失败或者没有开启磁盘缓存,返回的就是NULL.
     *
     * @return 配置使用的DiskLruCache
     */
    public DiskLruCache getDiskLruCache() {
        return diskLruCache;
    }

    /**
     * 创建DiskLruCache
     */
    public DiskLruCache recreateDiskCache() {
        try {
            File dir = WelikeContext.getDiskCacheDir(cacheDirName);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            diskLruCache = DiskLruCache.open(dir, AppUtils.getAppVersion(), 2, maxDiskCacheSize);
        } catch (IOException e) {
            if (debugMode) {
                WeLog.e("无法创建DiskLruCache,原因: " + e.getLocalizedMessage());
            }
        }
        return diskLruCache;
    }


    public HttpConfig() {
        cacheDirName = DEFAULT_CACHE_DIR_NAME;
        recreateDiskCache();
    }

    public HttpConfig(String cacheDirName) {
        this.cacheDirName = cacheDirName;
        recreateDiskCache();
    }

    /**
     * @param cacheDirName 缓存文件夹名
     * @param maxDiskCacheSize 缓存大小
     */
    public HttpConfig(String cacheDirName, long maxDiskCacheSize) {
        this.cacheDirName = cacheDirName;
        this.maxDiskCacheSize = maxDiskCacheSize;
        recreateDiskCache();
    }

    /**
     *
     * @param cacheDirName 缓存文件夹名
     * @param enableDiskCache 是否开启DiskLruCache缓存?
     */
    public HttpConfig(String cacheDirName, boolean enableDiskCache) {
        this.cacheDirName = cacheDirName;
        this.enableDiskCache = enableDiskCache;
        recreateDiskCache();
    }

    public HttpConfig(String cacheDirName, long maxDiskCacheSize, boolean enableDiskCache) {
        this.cacheDirName = cacheDirName;
        this.maxDiskCacheSize = maxDiskCacheSize;
        this.enableDiskCache = enableDiskCache;
    }


    /**
     * 得到当前时刻的缓存过期时间.
     *
     * @return 过期时间
     */
    public long generateTimeoutDate() {
        return expiryDate == 0 ? 0 : System.currentTimeMillis() + expiryDate;
    }



}
