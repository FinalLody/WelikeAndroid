package com.lody.welike.http;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 内部维护Http请求的参数
 *
 * @author Lody
 * @version 1.2
 */
public class HttpParams {

    /**
     * multipart/form-data类型的Content-Type
     */
    public static final String MULTI_PART_FORM_DATA = "multipart/form-data;";

    private Map<String, String> header = new HashMap<>();
    private Map<String, String> params = new HashMap<>();
    private Map<String, File> uploadFiles = new HashMap<>();

    /**
     * 代表我们规定的分割符，可以自己任意规定，
     * 但为了避免和正常文本重复了，尽量要使用复杂一点的内容.
     */
    public String boundary = "---------------------------7d33aim6d3lody";

    /**
     * 取得指定参数的值
     *
     * @param property
     * @return
     */
    public String getParam(String property) {
        return params.get(property);
    }

    /**
     * 取得一个请求头
     *
     * @param property
     * @return
     */
    public String getHeader(String property) {

        return header.get(property);
    }

    /**
     * 添加一个参数
     *
     * @param property
     * @param value
     * @return
     */
    public <T> HttpParams put(String property, T value) {
        params.put(property, String.valueOf(value));
        return this;
    }

    /**
     * 添加一个请求头
     *
     * @param property
     * @param value
     * @return
     */
    public <T> HttpParams putHeader(String property, T value) {
        header.put(property, String.valueOf(value));
        return this;
    }

    /**
     * 将一个Map(参数,值)全部添加带Params
     *
     * @param partParams
     * @return
     */
    public HttpParams putAllParams(Map<String, String> partParams) {
        params.putAll(partParams);
        return this;
    }

    /**
     * 将一个Map<参数,值>全部添加到Header
     *
     * @param partParams
     * @return
     */
    public HttpParams putAllHeader(Map<String, String> partParams) {
        header.putAll(partParams);
        return this;
    }

    /**
     * 添加一个以{key,value,key,value...}排列的数组到Params.
     *
     * @param args
     * @return
     */
    public <T> HttpParams putParams(@SuppressWarnings("unchecked") T... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Cannot parse the params.");
        }
        int N = 0;
        while (N < args.length) {
            put(String.valueOf(args[N]), args[++N]);
            N++;
        }

        return this;
    }

    /**
     * 添加一个文件(文件将被上传)
     *
     * @param property
     * @param file
     * @return
     */
    public HttpParams putFile(String property, File file) {

        uploadFiles.put(property, file);
        return this;
    }

    /**
     * 添加一个以{key,value,key,value...}排列的数组到Headers.
     *
     * @param args
     * @return
     */
    public HttpParams putHeaders(String... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Cannot parse the params.");
        }
        int N = 0;
        while (N < args.length) {
            putHeader(args[N], args[++N]);
            N++;
        }

        return this;
    }

    /**
     * 设置User-Agent属性
     *
     * @param userAgent
     * @return
     */
    public HttpParams setUserAgent(String userAgent) {
        putHeader("User-Agent", userAgent);
        return this;
    }

    /**
     * 设置请求头的Accept-Language
     *
     * @param acceptLanguage
     * @return
     */
    public HttpParams setAcceptLanguage(String acceptLanguage) {
        putHeader("Accept-Language", acceptLanguage);
        return this;
    }

    /**
     * 设置请求字符集
     *
     * @param charset
     * @return
     */
    public HttpParams setCharset(String charset) {
        putHeader("Charset", charset);
        return this;
    }

    /**
     * 一个响应头标，它允许服务器指明.
     * 将在给定的偏移和长度处，为资源组成部分的接受请求。
     * 该头标的值被理解为请求范围的度量单位。
     *
     * @param range
     * @return
     */
    public HttpParams setRange(String range) {
        putHeader("Range", range);
        return this;
    }

    /**
     * 定义客户端可以处理的MIME类型，按优先级排序；在一个以逗号为分隔的列表中，可以定义多种类型和使用通配符。
     *
     * @param acceptMime
     * @return
     */
    public HttpParams setAccept(String acceptMime) {
        putHeader("Accept", acceptMime);

        return this;
    }

    /**
     * 当服务器收到附带有Connection: Keep-Alive的请求时，
     * 它也会在响应头中添加一个同样的字段来使用Keep-Alive。
     * 这样一来，客户端和服务器之间的HTTP连接就会被保持，
     * 不会断开（超过Keep-Alive规定的时间，意外断电等情况除外），
     * 当客户端发送另外一个请求时，就使用这条已经建立的连接.
     *
     * @return
     */
    public HttpParams keepAlive() {
        putHeader("Connection", "keep-Alive");
        return this;
    }

    /**
     * 设置编码
     *
     * @param encoding
     * @return
     */
    public HttpParams setEncoding(String encoding) {
        putHeader("encoding", encoding);
        return this;
    }

    public Map<String, File> getUploadFiles() {
        return uploadFiles;
    }

    /**
     * @return 由所有参数的属性名构成的Set
     */
    public Set<String> paramKeySet() {
        return params.keySet();
    }

    /**
     * 由所有请求头的属性名构成的Set
     *
     * @return
     */
    public Set<String> headerKeySet() {
        return header.keySet();
    }

    /**
     * @return 参数数量
     */
    public int paramsSize() {
        return params.size();
    }

    /**
     * @return header数量
     */
    public int headersSize() {
        return header == null ? 0 : header.size();
    }


    /**
     * 为Http请求构造参数
     *
     * @param encode 编码
     * @return 连接起来的参数字符串
     */
    public String makeParams(String encode) {
        StringBuffer sb = new StringBuffer();
        HttpParams params = this;
        if (params != null && params.paramsSize() > 0) {
            for (String key : params.paramKeySet()) {
                try {
                    sb.append(key).append("=").append(URLEncoder.encode(params.getParam(key), encode)).append("&");
                } catch (UnsupportedEncodingException e) {
                }
            }
            sb.deleteCharAt(sb.length() - 1);//删掉最后一个&
        }

        return sb.toString();
    }
}
