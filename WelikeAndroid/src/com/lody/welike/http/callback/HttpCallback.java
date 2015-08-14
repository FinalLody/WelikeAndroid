package com.lody.welike.http.callback;

import com.lody.welike.http.HttpRequest;
import com.lody.welike.http.HttpResponse;

/**
 * Http请求的回调基础回调,
 * 你只需要复写你需要回调的那部分方法.
 * 上传文件时可以使用{@link FileUploadCallback}.
 *
 * @author lody
 */
public abstract class HttpCallback {

    /**
     * 开始请求前回调
     *
     * @param request
     */
    public void onPreRequest(HttpRequest request) {
    }

    /**
     * 请求成功后回调
     *
     * @param response
     */
    public void onSuccess(HttpResponse response) {
    }

    /**
     * 请求失败后回调
     *
     * @param response
     */
    public void onFailure(HttpResponse response) {
    }

    /**
     * 请求完成后回调(无论成功与否,都会回调)
     *
     * @param response
     */
    public void onFinish(HttpResponse response) {
    }

    /**
     * 请求取消时回调
     *
     * @param request
     */
    public void onCancel(HttpRequest request) {
    }


}
