package com.lody.welike.http;

import com.lody.welike.WelikeHttp;
import com.lody.welike.http.callback.HttpCallback;

import java.io.File;
import java.util.Map;

/**
 * @author Lody
 *         <br>
 *         Http请求构造器,通过本类能够轻松构造一个Http请求,
 *         通过{@link WelikeHttp}发送请求.
 */

public class HttpRequestBuilder {


    private String url;
    private RequestMethod method = RequestMethod.GET;
    private HttpConfig httpConfig;
    private HttpParams params;
    private HttpCallback callback;

    public HttpRequestBuilder(String url) {
        this.url = url;
    }

    /**
     * 创建一个RequestBuilder
     *
     * @param url 请求的目标Url
     * @return
     */
    public static HttpRequestBuilder newBuilder(String url) {

        return new HttpRequestBuilder(url);
    }

    /**
     * 配置请求类型
     *
     * @param requestMethod
     * @return
     */
    public HttpRequestBuilder requestMethod(RequestMethod requestMethod) {

        this.method = requestMethod;
        return this;
    }

    /**
     * 配置Http配置
     *
     * @param config
     * @return
     */

    public HttpRequestBuilder config(HttpConfig config) {

        this.httpConfig = config;
        return this;
    }

    /**
     * 配置HttpParam
     *
     * @param params
     * @return
     */
    public HttpRequestBuilder withParams(HttpParams params) {
        this.params = params;
        return this;
    }


    /**
     * 配置参数
     *
     * @param params
     * @return
     */
    public HttpRequestBuilder withParams(Map<String, String> params) {
        if (this.params == null) {
            this.params = new HttpParams();
        }
        this.params.putAllParams(params);

        return this;
    }

    /**
     * 添加需要上传的文件
     *
     * @param name
     * @param file
     * @return
     */
    public HttpRequestBuilder withFile(String name, File file) {

        if (this.params == null) {
            this.params = new HttpParams();
        }
        this.params.putFile(name, file);
        this.method = RequestMethod.POST;
        return this;
    }

    /**
     * 配置参数
     *
     * @param params
     * @return
     */
    public <T> HttpRequestBuilder withParams(@SuppressWarnings("unchecked") T... params) {
        if (this.params == null) {
            this.params = new HttpParams();
        }
        this.params.putParams(params);
        return this;
    }

    /**
     * 配置请求头
     *
     * @param headers
     * @return
     */
    public HttpRequestBuilder withHeaders(String... headers) {
        if (this.params == null) {
            this.params = new HttpParams();
        }
        this.params.putHeaders(headers);
        return this;
    }


    /**
     * 配置请求头
     *
     * @param headers
     * @return
     */
    public HttpRequestBuilder withHeaders(Map<String, String> headers) {
        if (this.params == null) {
            this.params = new HttpParams();
        }
        this.params.putAllHeader(headers);
        return this;
    }


    /**
     * 配置Http请求的回调
     *
     * @param callback
     * @return
     */
    public HttpRequestBuilder callback(HttpCallback callback) {
        this.callback = callback;
        return this;
    }

    /**
     * 构造一个{@link HttpRequest}实例.
     *
     * @return
     */
    public HttpRequest build() {
        if (this.url == null) {
            throw new IllegalArgumentException("URL没传入到HttpRequestBuilder.");
        }
        if (this.httpConfig == null) {
            httpConfig = HttpConfig.newDefaultConfig();
        }
        HttpSession session = HttpSessionManager.getManager().getSession(url, method);
        return new HttpRequest(session, params, httpConfig, callback);
    }
}
