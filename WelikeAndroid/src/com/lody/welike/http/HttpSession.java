package com.lody.welike.http;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * 代表一个Http的会话
 *
 * @author lody
 * @version 1.2
 */
public class HttpSession {

    //=============================================
    //                   字段
    //=============================================
    /**
     * 请求的URL
     */
    private String url;
    /**
     * 请求的方式
     */
    private RequestMethod requestMethod = RequestMethod.GET;


    /**
     * @param url
     * @param requestMethod
     */
    public HttpSession(String url, RequestMethod requestMethod) {
        this.url = url;
        this.requestMethod = requestMethod;
    }

    /**
     * 取得请求的URL
     *
     * @return
     */
    public String getUrl() {
        return url;
    }

    public String getFixUrl() {
        return url.startsWith("http://") ? url : "http://" + url;
    }

    /**
     * 设置请求的URL
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }


    /**
     * 取得请求的方式
     *
     * @return 请求方式
     */
    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    /**
     * 设置请求的方式
     *
     * @param requestMethod 请求方式
     */
    public void setRequestMethod(RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * 打开会话的网络连接
     *
     * @param request Http请求
     * @return 请求对应的Http连接
     * @throws IOException
     */
    public HttpURLConnection open(HttpRequest request) throws IOException {
        HttpURLConnection urlConnection;
        if (RequestMethod.GET == requestMethod) {
            String param = request.getParams().makeParams(request.getHttpConfig().getEncoding());
            String fullUrl = getFixUrl();
            if (param.length() > 1) {
                fullUrl += "?" + param;
            }

            urlConnection = (HttpURLConnection) new URL(fullUrl).openConnection();
        } else {
            urlConnection = (HttpURLConnection) new URL(getFixUrl()).openConnection();
            urlConnection.setUseCaches(false);
            urlConnection.setRequestMethod("POST");
        }
        request.getParams().setEncoding(request.getHttpConfig().getEncoding());

        Map<String, File> uploadFiles = request.getParams().getUploadFiles();
        if (uploadFiles.size() > 0) {
            if (requestMethod != RequestMethod.POST) {
                throw new IllegalArgumentException("请求需要上传文件,RequestMethod必须为Post方式!");
            }
            request.getParams().keepAlive().putHeader("Content-Type", HttpParams.MULTI_PART_FORM_DATA + " boundary=" + request.getParams().boundary);
        }

        if (request.getParams().headersSize() > 0) {
            for (String key : request.getParams().headerKeySet()) {
                urlConnection.addRequestProperty(key, request.getParams().getHeader(key));
            }
        }
        //我们有自己的缓存机制,所以那套就不需要了
        urlConnection.setUseCaches(false);
        //设置连接超时
        if (request.getConnectTimeout() != 0) {
            urlConnection.setConnectTimeout(request.getConnectTimeout());
        }
        //设置读取超时
        if (request.getReadTimeOut() != 0) {
            urlConnection.setReadTimeout(request.getReadTimeOut());
        }
        //设置是否允许重定向
        urlConnection.setAllowUserInteraction(request.getHttpConfig().isAllowUserInteraction());

        return urlConnection;
    }


    /**
     * 关闭会话
     */
    protected void close() {

    }

    /**
     * 结束会话
     */
    public void finish() {
        HttpSessionManager.getManager().finish(this);
        this.close();
    }


}
