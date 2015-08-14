package com.lody.welike.http;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Http响应的封装
 *
 * @author lody
 * @version 2.0
 */
public class HttpResponse implements Serializable {
  
	private static final long serialVersionUID = -2291797411702677590L;
	/**
     * 内容长度
     */
    public int contentLength;
    /**
     * 请求返回的数据
     */
    public byte[] data;
    /**
     * 响应代码
     */
    public int responseCode;
    /**
     * 响应信息
     */
    public String responseMessage;
    /**
     * 错误信息
     */
    public String errorMessage;
    /**
     * 内容的MIME类型
     */
    public String contentType;
    /**
     * 请求内容最后变化的时间
     */
    public long lastModifiedTime;
    /**
     * 请求的编码
     */
    public String contentEncoding;
    /**
     * 返回的响应头
     */
    public Map<String, List<String>> header;
    /**
     * Http响应对应的Http请求
     */
    public transient HttpRequest httpRequest;

    public void copyFrom(HttpResponse response) {
        this.contentLength = response.contentLength;
        this.contentEncoding = response.contentEncoding;
        this.contentType = response.contentType;
        this.data = response.data;
        this.errorMessage = response.errorMessage;
        copyHeader(response.header);
        this.responseCode = response.responseCode;
        this.lastModifiedTime = response.lastModifiedTime;
        this.responseMessage = response.responseMessage;
    }

    /**
     * 复制Header,转换为HashMap
     *
     * @param oldHeader
     */
    public void copyHeader(Map<String, List<String>> oldHeader) {
        //oldHeader的类型为RawHeader$1,是一个内部类,并且没有实现序列化接口,
        //因此将它转换为HashMap.
        this.header = new HashMap<>(oldHeader.size());
        for (String key : oldHeader.keySet()) {
            List<String> value = oldHeader.get(key);
            header.put(key, value);
        }
    }
}
