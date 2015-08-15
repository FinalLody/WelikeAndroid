package com.lody.sample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.lody.welike.WelikeBitmap;
import com.lody.welike.bitmap.BitmapPreprocessor;
import com.lody.welike.bitmap.callback.BitmapCallback;
import com.lody.welike.http.HttpRequest;
import com.lody.welike.http.HttpResponse;
import com.lody.welike.ui.WelikeActivity;
import com.lody.welike.ui.WelikeToast;
import com.lody.welike.ui.annotation.JoinView;
import com.lody.welike.utils.WeLog;



/**
 * @author Lody
 * @version 1.0
 */
public class BitmapActivity extends WelikeActivity {

    @JoinView(id = R.id.imageView)
    private ImageView imageView;

    @JoinView(name = "show_1",click = true)
    private Button show;
    @JoinView(name = "clear_cache",click = true)
    private Button clearCache;

    @Override
    public void initGlobalView(Bundle savedInstanceState) {
        super.initGlobalView(savedInstanceState);
        setContentView(R.layout.bitmap_layout);
    }


    @Override
    public void onWidgetClick(View widget) {
        super.onWidgetClick(widget);
        if (widget == show){
            //请注意观察Log

            //取得默认的WelikeBitmap实例
            WelikeBitmap welikeBitmap = WelikeBitmap.getDefault();
            welikeBitmap.loadBitmap(imageView,
                    "http://api.k780.com:88/?data=Welike-Framework&app=qr.get&size=6&level=l",
                    android.R.drawable.btn_star, android.R.drawable.ic_delete, new BitmapCallback() {

                @Override
                public Bitmap onProcessBitmap(byte[] data) {
                    return super.onProcessBitmap(data);
                }

                @Override
                public void onPreStart(String url) {
                    super.onPreStart(url);
                    WeLog.d("===========> onPreStart()");
                }

                @Override
                public void onCancel(String url) {
                    super.onCancel(url);
                    WeLog.d("===========> onCancel()");
                }

                @Override
                public void onLoadSuccess(String url, Bitmap bitmap) {
                    super.onLoadSuccess(url, bitmap);
                    WeLog.d("===========> onLoadSuccess()");
                }

                @Override
                public void onRequestHttp(HttpRequest request) {
                    super.onRequestHttp(request);
                    WeLog.d("===========> onRequestHttp()");
                }

                @Override
                public void onLoadFailed(HttpResponse response, String url) {
                    super.onLoadFailed(response, url);
                    WeLog.d("===========> onLoadFailed(),Reason:" + response.responseMessage);
                }
            });
        }else if (widget == clearCache){
            WelikeBitmap.getDefault().clearCache();
            WelikeBitmap.getDefault().getBitmapConfig().getMemoryLruCache().clearAllBitmap();
            WelikeToast.toast("缓存清理成功!");
        }

    }
}
