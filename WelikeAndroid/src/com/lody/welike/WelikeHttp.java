package com.lody.welike;

import com.lody.welike.http.DownloadController;
import com.lody.welike.http.HttpConfig;
import com.lody.welike.http.HttpParams;
import com.lody.welike.http.HttpRequest;
import com.lody.welike.http.HttpRequestQueue;
import com.lody.welike.http.HttpSession;
import com.lody.welike.http.HttpSessionManager;
import com.lody.welike.http.HttpThreadPool;
import com.lody.welike.http.RequestMethod;
import com.lody.welike.http.callback.DownloadCallback;
import com.lody.welike.http.callback.HttpCallback;
import com.lody.welike.utils.DiskLruCache;
import com.lody.welike.utils.WeLog;

import java.io.File;
import java.io.IOException;

/**
 * @author Lody
 *         <p/>
 *         本框架的Http请求核心类,封装了Http异步的API,
 *         你只需要一句话就可以完成Get,Post的简单请求.
 *         注意{@link HttpConfig}的合理配置.这可能会使框架发挥更好的性能.
 */
public class WelikeHttp {

    private static WelikeHttp INSTANCE;
    /**
     * Welike-Http 的<b>核心请求队列</b>,
     * 开启一个线程不断拉去Http请求,
     * 并派发执行请求的任务.
     */
    private HttpRequestQueue requestQueue = new HttpRequestQueue();

    /**
     * 下载控制器,保存所有的下载任务
     */
    private DownloadController downloadController = new DownloadController();
    /**
     * 储存配置信息.
     */
    private HttpConfig config;

    /**
     * 构造器,使用默认配置创建一个{@link WelikeHttp}实例.
     */
    public WelikeHttp() {
        this(HttpConfig.newDefaultConfig());

    }

    /**
     * 取得默认的WelikeHttp实例.我们强烈建议您使用本方法获取已创建的单例,
     * 因为创建多个{@link WelikeHttp}可能会产生不必要的问题.
     * 如果默认配置并不是你所想要的,你可以通过{@link WelikeHttp#getConfig()}修改配置.
     *
     * @return 默认的WelikeHttp实例
     */
    public static WelikeHttp getDefault() {
        if (INSTANCE == null) {
            synchronized (WelikeHttp.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WelikeHttp();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 默认构造器,根据{@link HttpConfig}创建一个{@link WelikeHttp}实例.
     *
     * @param config
     */
    public WelikeHttp(HttpConfig config) {
        this.config = config;
        HttpThreadPool.execute(requestQueue);
    }

    /**
     * 应用为另一个配置
     *
     * @param config
     */
    public void applyConfig(HttpConfig config) {
        this.config.applyConfig(config);
    }

    /**
     * 取得配置.
     *
     * @return 当前WelikeHttp实例的配置
     */
    public HttpConfig getConfig() {
        return this.config;
    }

    /**
     * 取得还未执行但即将执行的Http请求实例.
     *
     * @return 即将执行的HttpRequest实例.
     */
    public HttpRequest currentRequest() {
        return requestQueue.peekRequest();
    }

    /**
     * 发送一个异步Get请求
     *
     * @param url
     * @param callback
     */
    public HttpRequest get(String url, HttpCallback callback) {
        return get(url, null, callback);
    }

    /**
     * 发送一个异步Get请求
     *
     * @param url
     * @param params
     * @param callback
     */
    public HttpRequest get(String url, HttpParams params, HttpCallback callback) {
        HttpRequest request = makeRequest(RequestMethod.GET, url, params, callback);
        enqueue(request);
        return request;
    }

    /**
     * 发送一个异步Post请求
     *
     * @param url      请求的Url
     * @param callback 请求的回调
     */
    public HttpRequest post(String url, HttpCallback callback) {
        return post(url, null, callback);
    }


    /**
     * 发送一个异步Post请求,可以在params中添加需要上传的文件.
     *
     * @param url      请求的Url
     * @param params   请求的参数
     * @param callback 请求的回调
     */
    public HttpRequest post(String url, HttpParams params, HttpCallback callback) {
        HttpRequest request = makeRequest(RequestMethod.POST, url, params, callback);
        enqueue(request);
        return request;
    }

    /**
     * 从Url下载一个文件
     *
     * @param url      路径
     * @param target   下载到的地方
     * @param callback 回调
     * @return 下载控制器
     */
    public DownloadController download(String url, File target, DownloadCallback callback) {
        this.downloadController.startDownloadTask(url, target, callback).execute();
        return downloadController;
    }

    /**
     * 从Url下载一个文件
     *
     * @param url    路径
     * @param target 下载到的地方
     * @return 下载控制器
     */
    public DownloadController download(String url, File target) {
        this.downloadController.startDownloadTask(url, target, null).execute();
        return downloadController;
    }


    /**
     * 创建一个Http请求,仅在请求十分简单的情况下使用本方法,
     * 我们更建议你使用{@link com.lody.welike.http.HttpRequestBuilder}.
     *
     * @param requestMethod 请求方式[{@link RequestMethod#GET},{@link RequestMethod#POST)]
     * @param url           请求的目标Url
     * @param params        请求的参数
     * @param callback      请求的回调
     * @return
     */
    public HttpRequest makeRequest(RequestMethod requestMethod, String url, HttpParams params, HttpCallback callback) {
        HttpSession session = HttpSessionManager.getManager().getSession(url, requestMethod);
        HttpRequest request = new HttpRequest(session, params, config);
        request.setHttpCallback(callback);
        return request;
    }

    /**
     * 将一个Http请求送至队列,你可以调用{@link HttpRequest#cancel()}
     * 来取消一个请求.
     *
     * @param request 要放到队列的请求
     * @return
     */
    public WelikeHttp enqueue(HttpRequest request) {
        synchronized (requestQueue) {
            requestQueue.enqueue(request);
        }
        return this;
    }

    /**
     * 清除全部Http响应的缓存.
     */
    public void clearCache() {

        DiskLruCache cache = config.getDiskLruCache();
        if (cache != null) {
            try {
                synchronized (requestQueue) {
                    cache.delete();
                    //重新加载缓存
                    config.recreateDiskCache();
                }
            } catch (IOException e) {
                if (config.debugMode) {
                    WeLog.w("清除缓存失败,原因: " + e.getMessage());
                }
            }
        }
    }


    /**
     * 销毁WeLikeHttp实例<br>
     * <b>警告:慎用此方法</b>,本方法调用后将会释放全部的资源,Http请求执行队列也会被停止.
     * 调用本方法后请确定实例不会被继续使用,否则将抛出空指针.
     */
    public void destroy() {
        requestQueue.quit();
        HttpThreadPool.getThreadPoolExecutor().remove(requestQueue);
    }
}
