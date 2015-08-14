package com.lody.welike.http;

import com.lody.welike.http.callback.DownloadCallback;
import com.lody.welike.utils.ByteArrayPool;
import com.lody.welike.utils.MultiAsyncTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * 表示一个下载任务,支持下载进度显示
 *
 * @author Lody
 * @version 1.3
 */
public class DownloadTask extends MultiAsyncTask<Void, Integer, Boolean> {

    /**
     * 下载的Url
     */
    private String url;

    /**
     * 当前状态
     */
    private DownloadController.State currentState = DownloadController.State.NOT_START;

    /**
     * 下载回调
     */
    private List<DownloadCallback> callbacks = new ArrayList<>();

    /**
     * 下载到的位置
     */
    private File targetFile;

    /**
     * 下载控制器
     */
    private DownloadController controller;

    /**
     * 当前完成的进度
     */
    private int progress;
    /**
     * 是否已经取消
     */
    private boolean isCancel = false;

    /**
     * @param url
     * @param target
     * @param callback
     */
    public DownloadTask(DownloadController controller, String url, File target, DownloadCallback callback) {
        this.controller = controller;
        this.url = url;
        this.targetFile = target;
        this.callbacks.add(callback);
    }

    @Override
    public void onPrepare() {
        super.onPrepare();
        for (DownloadCallback callback : callbacks){
            callback.onDownloadStart(url);
        }
    }

    @Override
    public Boolean onTask(Void... urls) {
        currentState = DownloadController.State.DOWNLOADING;
        RandomAccessFile file;
        URL url;
        URLConnection connection;
        InputStream inputStream;
        int contentLength;
        try {
            file = new RandomAccessFile(targetFile, "rwd");
            file.seek(0);
            url = new URL(this.url.startsWith("http://") ? this.url : "http://" + this.url);
            connection = url.openConnection();
            inputStream = connection.getInputStream();
            contentLength = connection.getContentLength();
        } catch (Throwable e) {
            return false;
        }
        //使用字节数组缓冲池
        byte[] data = ByteArrayPool.get().getBuf(2048);

        try {
            int total = 0;
            int read;
            while ((read = inputStream.read(data)) != -1) {
                total += read;
                file.write(data, 0, read);
                progress = (total * 100) / contentLength;
                postUpdate(progress);
                //判断是否取消了下载
                if (isCancel) {
                    return false;
                }
            }
            //回收字节数组
            ByteArrayPool.get().returnBuf(data);

        } catch (IOException e) {
            return false;
        } finally {
            try {
                file.close();
                inputStream.close();
            } catch (IOException e) {
            }
        }


        return true;
    }

    @Override
    public void onResult(Boolean success) {
        super.onResult(success);
        if (success) {
            currentState = DownloadController.State.SUCCESS;
            for (DownloadCallback callback : callbacks){
                callback.onDownloadSuccess(url, targetFile);
            }

        } else if (isCancel) {
            currentState = DownloadController.State.CANCEL;
            for (DownloadCallback callback : callbacks){
                callback.onCancel(url);
            }
        } else {
            currentState = DownloadController.State.FAILED;
            for (DownloadCallback callback : callbacks){
                callback.onDownloadFailed(url);
            }
        }
        callbacks.clear();
        controller.finish(this);
    }

    @Override
    public void onUpdate(Integer integer) {
        super.onUpdate(integer);
        currentState = DownloadController.State.SUCCESS;
        for (DownloadCallback callback : callbacks){
            callback.onProgressUpdate(url,integer);
        }
    }

    /**
     * 取消下载任务
     */
    public void cancel() {
        this.isCancel = true;
    }

    /**
     * 任务是否已经取消?
     *
     * @return
     */
    public boolean isCancel() {
        return isCancel;
    }

    /**
     * @return 当前的下载状态
     */
    public DownloadController.State getCurrentState() {
        return currentState;
    }


    /**
     * 取得下载的完成进度
     *
     * @return 下载完成进度
     */
    public int getProgress() {
        return progress;
    }

    /**
     * @return 要下载的Url
     */
    public String getDownloadUrl() {
        return url;
    }

    public File getTargetFile() {
        return targetFile;
    }

    @Override
    public boolean equals(Object o) {
        return targetFile.getAbsolutePath().equals(((DownloadTask) o).getDownloadUrl());
    }

    /**
     * 添加一个回调
     * @param callback
     */
    public void addCallback(DownloadCallback callback){
        callbacks.add(callback);
    }
}
