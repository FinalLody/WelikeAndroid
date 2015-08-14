package com.lody.welike.http.callback;

import java.io.File;

/**
 * @author Lody
 * @version 1.0
 */
public abstract class DownloadCallback {

    /**
     * 当下载开始前回调
     *
     * @param url
     */
    public void onDownloadStart(String url) {
    }

    /**
     * 当下载进度更新时回调
     *
     * @param completed 完成的进度,总进度为100
     */
    public void onProgressUpdate(String url, int completed) {
    }

    /**
     * 当下载成功后回调
     *
     * @param url            下载的url
     * @param downloadedFile 下载成功生成的文件
     */
    public void onDownloadSuccess(String url, File downloadedFile) {
    }

    /**
     * 当下载失败后回调
     */
    public void onDownloadFailed(String url) {
    }

    /**
     * 下载任务取消时回调
     *
     * @param url 下载的url
     */
    public void onCancel(String url) {

    }
}
