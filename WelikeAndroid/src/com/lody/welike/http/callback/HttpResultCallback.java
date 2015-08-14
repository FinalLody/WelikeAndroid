package com.lody.welike.http.callback;

/**
 * 如果只关注请求结果的内容,可以选择使用本类.
 *
 * @author Lody
 * @version 1.0
 */
public abstract class HttpResultCallback extends HttpCallback {

    /**
     * 请求成功时返回的响应结果文本内容
     *
     * @param content
     */
    public void onSuccess(String content) {
    }

}
