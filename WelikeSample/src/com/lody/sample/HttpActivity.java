package com.lody.sample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.lody.welike.WelikeHttp;
import com.lody.welike.http.HttpParams;
import com.lody.welike.http.HttpRequest;
import com.lody.welike.http.HttpResponse;
import com.lody.welike.http.callback.DownloadCallback;
import com.lody.welike.http.callback.HttpCallback;
import com.lody.welike.http.callback.HttpResultCallback;
import com.lody.welike.ui.WelikeActivity;
import com.lody.welike.ui.WelikeToast;
import com.lody.welike.ui.annotation.JoinView;
import java.io.File;

/**
 * @author Lody
 * @version 1.0
 */
public class HttpActivity extends WelikeActivity {

    @JoinView(id = R.id.get,click = true)
    Button getButton;
    @JoinView(id = R.id.post,click = true)
    Button postButton;
    @JoinView(name = "jsonGet",click = true)
    Button jsonGetButton;
    @JoinView(id = R.id.download,click = true)
    Button downloadButton;
    @JoinView(name = "progressBar",click = true)
    ProgressBar progressBar;
    @Override
    public void initGlobalView(Bundle savedInstanceState) {
        super.initGlobalView(savedInstanceState);
        setContentView(R.layout.http_layout);
    }

    @Override
    public void onWidgetClick(View widget) {
        super.onWidgetClick(widget);
        if (widget == getButton){
            getEvent();
        }else if (widget == postButton){
           postEvent();
        }else if (widget == jsonGetButton){
            jsonGetEvent();

        }else if (widget == downloadButton){
           downloadEvent();
        }
    }

    /**
     * 发送一个Post请求
     */
    private void postEvent() {
        WelikeHttp.getDefault().post("www.baidu.com", new HttpCallback() {
            @Override
            public void onSuccess(HttpResponse response) {
                super.onSuccess(response);
                WelikeToast.toast("请求成功!");
            }

            @Override
            public void onFailure(HttpResponse response) {
                super.onFailure(response);
                WelikeToast.toast("请求失败!");
            }
        });
    }

    /**
     * 发送一个JSON Get请求
     */
    private void jsonGetEvent() {
        HttpParams params = new HttpParams();
        params.put("app","qr.get");
        WelikeHttp.getDefault().get("http://api.k780.com:88", params, new HttpResultCallback() {
            @Override
            public void onSuccess(String content) {
                super.onSuccess(content);
                WelikeToast.toast("返回的JSON为:" + content);
            }

            @Override
            public void onFailure(HttpResponse response) {
                super.onFailure(response);
                WelikeToast.toast("JSON请求发送失败.");
            }

            @Override
            public void onCancel(HttpRequest request) {
                super.onCancel(request);
            }
        });
    }

    private void downloadEvent() {
        new AlertDialog.Builder(this)
                .setMessage("你确定要下载百度音乐吗?")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WelikeHttp.getDefault()
                                .download("http://123.125.110.15/dd.myapp.com/16891/FCDA105FD408930C58D0145D400F2447.apk?mkey=55cb21cc52a9a1c8&f=d410&fsname=com.ting.mp3.android_5.6.2.8_5628.apk&asr=02f1&p=.apk",
                                        new File(Environment.getExternalStorageDirectory(), "bdMusic.apk"),
                                        new DownloadCallback() {
                                            @Override
                                            public void onDownloadStart(String url) {
                                                super.onDownloadStart(url);
                                                WelikeToast.toast("下载开始...");
                                            }

                                            @Override
                                            public void onProgressUpdate(String url, int completed) {
                                                super.onProgressUpdate(url, completed);
                                                progressBar.setProgress(completed);
                                            }

                                            @Override
                                            public void onDownloadSuccess(String url, File downloadedFile) {
                                                super.onDownloadSuccess(url, downloadedFile);
                                                WelikeToast.toast("下载成功,文件已保存到 : " + downloadedFile.getParent());
                                            }

                                            @Override
                                            public void onDownloadFailed(String url) {
                                                super.onDownloadFailed(url);
                                                WelikeToast.toast("下载失败 T_T");
                                            }
                                        }
                                );
                    }
                }).show();
    }

    public void getEvent() {
        WelikeHttp welikeHttp = WelikeHttp.getDefault();
        welikeHttp.get("www.baidu.com", new HttpCallback() {
            @Override
            public void onSuccess(HttpResponse response) {
                super.onSuccess(response);
                WelikeToast.toast("Get请求成功!");
            }

            @Override
            public void onFailure(HttpResponse response) {
                super.onFailure(response);
                WelikeToast.toast("Post请求失败!");
            }
        });
    }
}
