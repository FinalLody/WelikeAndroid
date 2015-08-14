package com.lody.welike.http.callback;

import android.graphics.Bitmap;

/**
 * 当你需要使用Http请求加载图片时使用本回调.
 *
 * @author Lody
 * @version 1.0
 */
public abstract class HttpBitmapCallback extends HttpCallback {

    /**
     * 当一张图片下载成功时回调
     *
     * @param bitmap
     */
    public void onSuccess(Bitmap bitmap) {
    }

    /**
     * 处理图片时回调,可以自己定义如何加载图片
     * @param data
     * @return
     */
    public Bitmap onProcessBitmap(byte[] data){
        return null;
    }
}
