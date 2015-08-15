package com.lody.welike.http;

import com.lody.welike.http.callback.DownloadCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 下载控制器,
 * 要取得下载进度,请使用{@link DownloadTask#getProgress()}取得下载进度.
 *
 * @author Lody
 * @version 1.4
 */
public class DownloadController {

    /**
     * 目前的下载任务
     */
    private List<DownloadTask> downloadTasks = Collections.synchronizedList(new ArrayList<DownloadTask>());
    /**
     * 当下载的任务完成后,就会从downloadTasks中移除,并将状态加入到本表中.
     */
    private Map<String, State> finishUrlToState = new ConcurrentHashMap<>();

    /**
     * 取得指定Url的下载状态
     *
     * @param url
     * @return
     */
    public State getDownloadState(String url) {
        for (DownloadTask downloadTask : downloadTasks) {
            if (downloadTask.getDownloadUrl().equals(url)) {
                return downloadTask.getCurrentState();
            }
        }
        for (String finishUrl : finishUrlToState.keySet()) {
            if (finishUrl.equals(url)) {
                return finishUrlToState.get(finishUrl);
            }
        }
        return State.NONE;
    }

    /**
     * 取得下载任务
     *
     * @param url
     * @return
     */
    public DownloadTask getDownloadTask(String url) {

        for (DownloadTask downloadTask : downloadTasks) {
            if (downloadTask.getDownloadUrl().equals(url)) {
                return downloadTask;
            }
        }
        return null;
    }

    /**
     * 开始一个下载任务,如果任务已经存在,直接返回任务
     *
     * @param url      下载Url
     * @param target   下载到的地方
     * @param callback 回调
     * @return
     */
    public DownloadTask startDownloadTask(String url, File target, DownloadCallback callback) {

        for (DownloadTask downloadTask : downloadTasks) {
            if (downloadTask.getTargetFile().equals(target)) {
                //如果下载任务存在,就返回下载任务
                downloadTask.addCallback(callback);
                return downloadTask;
            }
        }
        //创建下载任务
        DownloadTask downloadTask = new DownloadTask(this, url, target, callback);
        //将下载任务放到任务列表
        downloadTasks.add(downloadTask);

        return downloadTask;
    }


    /**
     * 取消指定Url的下载任务
     *
     * @param url 指定的Url
     */
    public void cancelDownloadTask(String url) {
        DownloadTask task = getDownloadTask(url);
        if (task != null){
            task.cancel();
        }
    }

    /**
     * 结束一个下载任务
     *
     * @param task
     */
    public void finish(DownloadTask task) {
        downloadTasks.remove(task);
        finishUrlToState.put(task.getDownloadUrl(), task.getCurrentState());
    }

    /**
     * 下载状态
     *
     * @author Lody
     */
    public static enum State {
        /**
         * 下载中的状态
         */
        DOWNLOADING,
        /**
         * 下载成功状态
         */
        SUCCESS,
        /**
         * 下载失败的状态
         */
        FAILED,
        /**
         * 还没开始下载
         */
        NOT_START,
        /**
         * 任务不存在
         */
        NONE,
        /**
         * 下载任务已经取消
         */
        CANCEL

    }

}
