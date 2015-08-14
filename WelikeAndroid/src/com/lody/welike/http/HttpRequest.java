package com.lody.welike.http;

import com.lody.welike.http.callback.FileUploadCallback;
import com.lody.welike.http.callback.HttpCallback;
import com.lody.welike.utils.DiskLruCache;
import com.lody.welike.utils.HashUtils;
import com.lody.welike.utils.IOUtils;
import com.lody.welike.utils.WeLog;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Http请求的封装
 *
 * @author Lody
 * @version 2.1
 */
public class HttpRequest {

    //=============================================
    //                   字段
    //=============================================

    /**
     * Http请求需要的参数
     */
    private HttpParams params;

    /**
     * Http请求的配置
     */
    private HttpConfig httpConfig;

    /**
     * Http会话
     */
    private HttpSession session;

    /**
     * 请求是否已经取消
     */
    private boolean isCancel;
    /**
     * Http请求的回调
     */
    private HttpCallback httpCallback;

    /**
     * 读取超时时间
     */
    private int readTimeOut;

    /**
     * 连接超时时间
     */
    private int connectTimeout;

    /**
     * 用于DiskLruCache缓存的Key
     */
    private String cacheKey;


    /**
     * @param session
     * @param params
     * @param httpConfig
     */
    public HttpRequest(HttpSession session, HttpParams params, HttpConfig httpConfig) {
        this.session = session;
        this.params = params;
        this.httpConfig = httpConfig;
    }

    /**
     * @param session
     * @param params
     * @param httpConfig
     * @param callback
     */
    public HttpRequest(HttpSession session, HttpParams params, HttpConfig httpConfig, HttpCallback callback) {
        this.session = session;
        this.params = params;
        this.httpConfig = httpConfig;
        this.httpCallback = callback;
    }


    /**
     * 取得Http请求的参数
     *
     * @return
     */
    public HttpParams getParams() {
        if (params == null) {
            params = new HttpParams();
        }
        return params;
    }

    /**
     * 设置Http参数
     *
     * @param params
     */
    public void setParams(HttpParams params) {
        this.params = params;
    }

    /**
     * 得到Http会话
     *
     * @return
     */
    public HttpSession getSession() {
        return session;
    }

    /**
     * 取消当前请求
     */
    public void cancel() {
        isCancel = true;
    }

    /**
     * 是否已经取消?
     *
     * @return
     */
    public boolean isCancel() {
        return isCancel;
    }

    /**
     * 设置Http请求的回调
     *
     * @param httpCallback 回调
     */
    public void setHttpCallback(HttpCallback httpCallback) {
        this.httpCallback = httpCallback;
    }

    /**
     * @return Http请求的回调
     */
    public HttpCallback getHttpCallback() {
        return httpCallback;
    }

    /**
     * 取得可配置的配置信息
     *
     * @return 可配置的配置信息
     */
    public HttpConfig getHttpConfig() {
        return httpConfig;
    }

    /**
     * 让当前请求的缓存过期
     */
    public void outOfCacheDate() {

        DiskLruCache cache = httpConfig.getDiskLruCache();
        if (cache != null) {
            String cacheKey = getCacheKey();
            String hashKey = HashUtils.hashKey(cacheKey);
            try {
                synchronized (this) {
                    DiskLruCache.Snapshot snapshot = cache.get(hashKey);
                    if (snapshot != null) {
                        DiskLruCache.Editor editor = snapshot.edit();
                        if (editor != null) {
                            //将到期时间改为当前时间,就等于马上过期了
                            editor.set(1, String.valueOf(System.currentTimeMillis()));
                            editor.commit();
                            snapshot.close();
                            cache.flush();
                            if (getHttpConfig().debugMode) {
                                WeLog.d(cacheKey + " 的缓存已强制过期.");
                            }
                        }
                    }
                }
            } catch (IOException e) {
                if (getHttpConfig().debugMode) {
                    WeLog.e("无法更新缓存过期时间,原因:" + e.getMessage());
                }
            }
        }

    }

    /**
     * 取得读取超时时间.
     *
     * @return
     */
    public int getReadTimeOut() {
        return readTimeOut;
    }

    /**
     * 设置Http读取延时.
     *
     * @param readTimeOut
     */
    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    /**
     * 取得连接超时时间.
     *
     * @return
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * 设置Http连接延时
     *
     * @param connectTimeout
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }


    public void writeToStream(DataOutputStream outputStream) {

        boolean isUpload = params.getUploadFiles().size() > 0;
        if (isUpload) {
            writeToStreamUploadMode(outputStream);
        } else {
            writeToStreamNormal(outputStream);
        }

    }

    /**
     * @return 用于DiskLruCache缓存的Key
     */
    public String getCacheKey() {
        if (cacheKey == null) {
            cacheKey = getSession().getFixUrl();
            String paramStatement = params == null ? "" : params.makeParams(getHttpConfig().getEncoding());
            if (paramStatement != null && paramStatement.length() > 1) {
                cacheKey += "?" + paramStatement;
            }
        }
        return cacheKey;

    }

    /**
     * 普通模式写入Post参数
     *
     * @param outputStream
     */
    private void writeToStreamNormal(DataOutputStream outputStream) {
        String param = params.makeParams(getHttpConfig().getEncoding());
        try {
            outputStream.write(param.getBytes(getHttpConfig().getEncoding()));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传模式写入Post输出流
     *
     * @param outputStream
     */
    private void writeToStreamUploadMode(DataOutputStream outputStream) {

        //遍历需要上传的文件
        for (String param : params.getUploadFiles().keySet()) {
            File file = params.getUploadFiles().get(param);
            FileUploadCallback uploadCallback = null;

            if (httpCallback != null && (httpCallback instanceof FileUploadCallback)) {
                uploadCallback = (FileUploadCallback) httpCallback;
            }
            if (uploadCallback != null) {
                uploadCallback.onFileStartUpload(this, file);
            }
            try {
                //写入分割线
                outputStream.writeBytes("--" + params.boundary + "\r\n");
                //写入文件表单描述
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + param + "\"; filename=\"" + URLEncoder.encode(file.getName(), httpConfig.getEncoding()) + "\"\r\n");
                //写入文件类型
                outputStream.writeBytes("Content-Type: " + getContentType(file) + "\r\n");
                outputStream.writeBytes("\r\n");
                //写入文件
                outputStream.write(IOUtils.getBytes(file));
                outputStream.writeBytes("\r\n");
                //写入尾
                writeEnd(outputStream);
                //刷新流
                outputStream.flush();
                if (uploadCallback != null) {
                    uploadCallback.onFileUploadSuccess(this, file);
                }
            } catch (Throwable e) {
                if (uploadCallback != null) {
                    uploadCallback.onFileUploadFailed(e.getMessage(), file);
                }
            }
        }

        for (String param : params.paramKeySet()) {
            String value = params.getParam(param);
            try {
                outputStream.writeBytes("--" + params.boundary + "\r\n");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + param + "\"\r\n");
                outputStream.writeBytes("\r\n");
                outputStream.writeBytes(URLEncoder.encode(value, httpConfig.getEncoding()) + "\r\n");
                outputStream.flush();
            } catch (IOException e) {
                if (httpConfig.debugMode) {
                    WeLog.e("写入参数出错,原因: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 写入结尾标识.
     *
     * @param outputStream
     */
    private void writeEnd(DataOutputStream outputStream) {
        try {
            outputStream.writeBytes("--" + params.boundary + "--" + "\r\n");
            outputStream.writeBytes("\r\n");
        } catch (IOException e) {
            WeLog.e("写入结尾表示出错,原因: " + e.getMessage());
        }
    }

    /**
     * 取得文件的ContentType
     *
     * @param file
     * @return
     */
    private String getContentType(File file) {

        return "application/octet-stream";
    }
}
