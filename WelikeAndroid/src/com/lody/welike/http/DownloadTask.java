package com.lody.welike.http;

import android.os.*;
import android.os.Process;

import com.lody.welike.guard.UncaughtThrowable;
import com.lody.welike.http.callback.DownloadCallback;
import com.lody.welike.utils.ByteArrayPool;
import com.lody.welike.utils.MultiAsyncTask;
import com.lody.welike.utils.WeLog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 表示一个下载任务,支持下载进度显示
 *
 * @author Lody
 * @version 1.3
 */
public class DownloadTask extends MultiAsyncTask<Void, Integer, DownloadController.State> {

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
    private Set<DownloadCallback> callbacks = new HashSet<>();

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
     * 是否已经暂停
     */
    private boolean isPause = false;

    private int finishedLength = 0;



    /**
     * @param url
     * @param target
     * @param callbacks
     */
    public DownloadTask(DownloadController controller, String url, File target, DownloadCallback... callbacks) {
        this.controller = controller;
        this.url = url;
        this.targetFile = target;
        for (DownloadCallback callback : callbacks){
            this.callbacks.add(callback);
        }
    }


    /**
     * @param url
     * @param target
     * @param callbacks
     */
    public DownloadTask(DownloadController controller, String url, File target, Set<DownloadCallback> callbacks) {
        this.controller = controller;
        this.url = url;
        this.targetFile = target;
        for (DownloadCallback callback : callbacks){
            this.callbacks.add(callback);
        }
    }



    @Override
    public void onPrepare() {
        super.onPrepare();
        for (DownloadCallback callback : callbacks){
            callback.onDownloadStart(url);
        }
    }

    @Override
    public DownloadController.State onTask(Void... urls) {
        currentState = DownloadController.State.DOWNLOADING;
        RandomAccessFile file;
        URL url;
        URLConnection connection;
        InputStream inputStream;
        int contentLength;
        try {
            file = new RandomAccessFile(targetFile, "rwd");
            url = new URL(this.url.startsWith("http://") ? this.url : "http://" + this.url);
            connection = url.openConnection();
            contentLength = connection.getContentLength();

            inputStream = connection.getInputStream();
        } catch (Throwable e) {
            e.printStackTrace();
            return DownloadController.State.FAILED;
        }
        //使用字节数组缓冲池
        byte[] data = ByteArrayPool.get().getBuf(2048);

        int oldProgress = 0;
        try {
            int read;
            while ((read = inputStream.read(data)) != -1) {
                finishedLength += read;
                file.write(data, 0, read);
                progress = finishedLength / ((int)(contentLength / 100.f + 0.5f));
                if (progress - oldProgress >= 1) {
                    oldProgress = progress;
                    postUpdate(progress);
                }
                //判断是否取消了下载
                if (isCancel) {
                    return DownloadController.State.CANCEL;
                }
                if (isPause){
                    return DownloadController.State.PAUSE;
                }
            }
            //回收字节数组
            ByteArrayPool.get().returnBuf(data);

        } catch (IOException e) {
            e.printStackTrace();
            return DownloadController.State.FAILED;
        } finally {
            try {
                file.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return DownloadController.State.SUCCESS;
    }

    @Override
    public void onResult(DownloadController.State result) {
        super.onResult(result);
        this.currentState = result;

        if (result == DownloadController.State.SUCCESS) {
            for (DownloadCallback callback : callbacks){
                callback.onDownloadSuccess(url, targetFile);
                callbacks.clear();
                controller.finish(this);
            }

        } else if (result == DownloadController.State.CANCEL) {
            for (DownloadCallback callback : callbacks){
                callback.onCancel(url);
                callbacks.clear();
                controller.finish(this);
            }
        } else if (result == DownloadController.State.FAILED){
            for (DownloadCallback callback : callbacks){
                callback.onDownloadFailed(url);
                callbacks.clear();
                controller.finish(this);
            }
        }else if (result == DownloadController.State.PAUSE){
            WeLog.d(":::::::::Paused**");
            for (DownloadCallback callback : callbacks) {
                callback.onPause(url);
            }
        }

    }

    @Override
    public void onUpdate(Integer integer) {
        super.onUpdate(integer);
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

    /**
     * 添加一个回调
     * @param callback
     */
    public void addCallback(DownloadCallback callback){
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }


    public int getFinishedLength() {
        return finishedLength;
    }

    public void setFinishedLength(int finishedLength) {
        this.finishedLength = finishedLength;
    }


    /**
     * 暂停
     */
    public void pause(){
        isPause = true;

    }

    public void resume(){
        if (isPause){
            execute();
        }
    }

    public Set<DownloadCallback> getCallbacks() {
        return callbacks;
    }
}
