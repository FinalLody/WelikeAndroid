package com.lody.welike.http.callback;

import com.lody.welike.http.HttpRequest;

import java.io.File;

/**
 * @author Lody
 * @version 1.0
 */
public class FileUploadCallback extends HttpCallback {

    /**
     * 当一个文件开始上传时回调
     *
     * @param request
     * @param file
     */
    public void onFileStartUpload(HttpRequest request, File file) {
    }

    /**
     * 当一个文件上传成功时回调.
     *
     * @param request
     * @param file
     */
    public void onFileUploadSuccess(HttpRequest request, File file) {
    }

    /**
     * 当一个文件上传失败时回调.
     *
     * @param errorMessage 错误消息
     * @param file         上传失败的文件
     */
    public void onFileUploadFailed(String errorMessage, File file) {
    }

}
