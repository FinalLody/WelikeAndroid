package com.lody.welike.bitmap.callback;

import android.graphics.Bitmap;

import com.lody.welike.http.HttpRequest;
import com.lody.welike.http.HttpResponse;

/**
 * 图片加载的回调
 *
 * @author Lody
 * @version 1.0
 */
public abstract class BitmapCallback {

    /**
     * 图片开始加载前回调
     *
     * @param url 要加载的图片的Url
     */
    public void onPreStart(String url) {
    }


    /**
     * 图片加载请求被取消时回调
     *
     * @param url 被取消的图片Url
     */
    public void onCancel(String url) {
    }

    /**
     * 图片加载成功后回调
     *
     * @param bitmap 加载成功的Bitmap
     */
    public void onLoadSuccess(String url, Bitmap bitmap) {
    }

    /**
     * 图片加载需要请求网络时回调
     *
     * @param request 请求
     */
    public void onRequestHttp(HttpRequest request) {
    }

    /**
     * 图片加载失败后回调
     *
     * @param response 网络响应
     * @param url      加载失败的图片Url
     */
    public void onLoadFailed(HttpResponse response, String url) {
    }

    /**
     * 需要将字节数组转换为Bitmap时回调
     * @param data
     * @return 可以为null, 也可以为自己处理后的Bitmap
     */
    public Bitmap onProcessBitmap(byte[] data){
        return null;
    }
}
