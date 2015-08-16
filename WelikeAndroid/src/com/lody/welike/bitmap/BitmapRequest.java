package com.lody.welike.bitmap;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.lody.welike.bitmap.callback.BitmapCallback;

/**
 * 封装一个图片加载的请求
 * @author Lody
 * @version 1.0
 */
public class BitmapRequest {
    /**
     * 需要加载图片的View
     */
    private View view;
    /**
     * 图片的Url
     */
    private String url;
    /**
     * 加载中的显示的图片
     */
    private Bitmap loadingBitmap;
    /**
     * 错误时显示的图片
     */
    private Bitmap errorBitmap;
    /**
     * 加载中显示的图片
     */
    private Drawable loadingDrawable;
    /**
     * 错误时显示的图片
     */
    private Drawable errorDrawable;
    /**
     * 图片加载回调
     */
    private BitmapCallback callback;

    /**
     * 图片要求的宽度
     */
    private int requestWidth;
    /**
     * 图片要求的高度
     */
    private int requestHeight;

    /**
     * 任务是否已经取消
     */
    private boolean isCancel;

    /**
     * @param view
     * @param url
     * @param requestWidth
     * @param requestHeight
     * @param loadingBitmap
     * @param errorBitmap
     * @param callback
     */
    public BitmapRequest(View view, String url, int requestWidth, int requestHeight, Bitmap loadingBitmap, Bitmap errorBitmap, BitmapCallback callback) {
        this.view = view;
        this.url = url;
        this.requestWidth = requestWidth;
        this.requestHeight = requestHeight;
        this.loadingBitmap = loadingBitmap;
        this.errorBitmap = errorBitmap;
        this.callback = callback;
    }

    /**
     * @param view
     * @param url
     * @param requestWidth
     * @param requestHeight
     * @param loadingDrawable
     * @param errorDrawable
     * @param callback
     */
    public BitmapRequest(View view, String url, int requestWidth, int requestHeight, Drawable loadingDrawable, Drawable errorDrawable, BitmapCallback callback) {
        this.view = view;
        this.url = url;
        this.requestWidth = requestWidth;
        this.requestHeight = requestHeight;
        this.loadingDrawable = loadingDrawable;
        this.errorDrawable = errorDrawable;
        this.callback = callback;
    }

    public Bitmap getLoadingBitmap() {
        return loadingBitmap;
    }

    public void setLoadingBitmap(Bitmap loadingBitmap) {
        this.loadingBitmap = loadingBitmap;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Bitmap getErrorBitmap() {
        return errorBitmap;
    }

    public void setErrorBitmap(Bitmap errorBitmap) {
        this.errorBitmap = errorBitmap;
    }

    public BitmapCallback getCallback() {
        return callback;
    }

    public void setCallback(BitmapCallback callback) {
        this.callback = callback;
    }

    public int getRequestWidth() {
        return requestWidth;
    }

    public void setRequestWidth(int requestWidth) {
        this.requestWidth = requestWidth;
    }

    public int getRequestHeight() {
        return requestHeight;
    }

    public void setRequestHeight(int requestHeight) {
        this.requestHeight = requestHeight;
    }


    public boolean isCancel() {
        return isCancel;
    }

    public void cancel() {
        this.isCancel = true;
    }

    public Drawable getLoadingDrawable() {
        return loadingDrawable;
    }

    public void setLoadingDrawable(Drawable loadingDrawable) {
        this.loadingDrawable = loadingDrawable;
    }

    public Drawable getErrorDrawable() {
        return errorDrawable;
    }

    public void setErrorDrawable(Drawable errorDrawable) {
        this.errorDrawable = errorDrawable;
    }
}
