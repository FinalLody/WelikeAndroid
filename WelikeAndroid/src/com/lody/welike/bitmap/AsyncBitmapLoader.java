package com.lody.welike.bitmap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import com.lody.welike.WelikeHttp;
import com.lody.welike.bitmap.callback.BitmapCallback;
import com.lody.welike.http.HttpConfig;
import com.lody.welike.http.HttpRequest;
import com.lody.welike.http.HttpRequestBuilder;
import com.lody.welike.http.HttpResponse;
import com.lody.welike.http.RequestMethod;
import com.lody.welike.http.callback.HttpBitmapCallback;
import com.lody.welike.utils.UiHandler;
import com.lody.welike.utils.WeLog;

/**
 * 图片异步加载器,
 * 复用了{@link WelikeHttp}的响应缓存机制.
 *
 * @author Lody
 * @version 1.0
 */
public class AsyncBitmapLoader implements Runnable {

    /**
     * 图片包裹请求队列.
     */
    private BlockingQueue<BitmapPackage> packageBlockingQueue = new LinkedBlockingDeque<>();
    /**
     * 是否停止队列拉取?
     */
    private boolean mQuit = false;

    private HttpConfig config;

    public AsyncBitmapLoader() {
        config = new HttpConfig(BitmapConfig.CACHE_DIR_NAME);
    }

    /**
     * 图片包裹,保存一起图片加载请求,交于队列
     */
    public static class BitmapPackage {
        /**
         * 图片的配置
         */
        BitmapConfig config;
        /**
         * 图片加载对应的请求
         */
        BitmapRequest request;

        public BitmapPackage(BitmapConfig config, BitmapRequest request) {
            this.config = config;
            this.request = request;
        }
    }

    @Override
    public void run() {

        for (; ; ) {
            try {
                final BitmapPackage bitmapPackage = packageBlockingQueue.take();
                final BitmapCallback callback = bitmapPackage.request.getCallback();
                final String url = bitmapPackage.request.getUrl();
                final boolean debug = bitmapPackage.config.debugMode;
                //请求是否已经取消
                if (bitmapPackage.request.isCancel()) {
                    if (debug) WeLog.d(url + "请求已经取消.");
                    UiHandler.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onCancel(url);
                            }
                        }
                    });
                    continue;
                }
                //回调onPreStart
                if (debug) WeLog.d(url + " 开始加载.");
                UiHandler.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onPreStart(url);
                        }
                    }
                });

                final Bitmap bitmap = bitmapPackage.config.getMemoryLruCache().getBitmap(url);
                //尝试从内存中拿Bitmap
                if (bitmap != null && !bitmap.isRecycled()) {
                    if (debug) WeLog.d("内存中命中一个Bitmap!");
                    UiHandler.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setImageToView(bitmap, bitmapPackage.request.getView());
                            if (callback != null) {
                                callback.onLoadSuccess(url, bitmap);
                            }
                        }
                    });
                    continue;
                } else {
                    if (debug) WeLog.d("内存中没有找到该Bitmap,开始Http请求...");
                }

                final HttpRequest request = HttpRequestBuilder.newBuilder(url)
                        .requestMethod(RequestMethod.GET)
                        .config(config)
                        .callback(new HttpBitmapCallback() {

                            @Override
                            public Bitmap onProcessBitmap(byte[] data) {

                                Bitmap decodeBitmap = null;
                                if (callback != null){
                                    decodeBitmap = callback.onProcessBitmap(data);
                                }
                                if (decodeBitmap == null) {
                                    View view = bitmapPackage.request.getView();
                                    if (view == null) {
                                        decodeBitmap = BitmapPreprocessor.decodeBitmapNoOOM(data);
                                    } else {
                                        int reqWidth = view.getWidth();
                                        int reqHeight = view.getHeight();
                                        decodeBitmap = BitmapPreprocessor.decodeBitmapNoOOM(data,reqWidth,reqHeight);
                                    }

                                }
                                return decodeBitmap;
                            }

                            @Override
                            public void onSuccess(Bitmap bitmap) {
                                super.onSuccess(bitmap);

                                int width = bitmapPackage.request.getRequestWidth();
                                int height = bitmapPackage.request.getRequestHeight();
                                int originWidth = bitmap.getWidth();
                                int originHeight = bitmap.getHeight();

                                bitmap = BitmapPreprocessor.zoomBitmap(bitmap, width == 0 ? originWidth : width, height == 0 ? originHeight : height);
                                bitmapPackage.config.getMemoryLruCache().putBitmap(url, bitmap);
                                setImageToView(bitmap, bitmapPackage.request.getView());
                                if (callback != null) {
                                    callback.onLoadSuccess(url, bitmap);
                                }
                            }

                            @Override
                            public void onPreRequest(HttpRequest request) {
                                super.onPreRequest(request);

                                Bitmap loadingBitmap = bitmapPackage.request.getLoadingBitmap();
                                Drawable loadingDrawable = bitmapPackage.request.getLoadingDrawable();
                                if (loadingBitmap != null) {
                                    setImageToView(loadingBitmap, bitmapPackage.request.getView());
                                } else if (loadingDrawable != null) {
                                    setImageToView(loadingDrawable, bitmapPackage.request.getView());
                                }
                                if (callback != null) {
                                    callback.onRequestHttp(request);
                                }
                            }

                            @Override
                            public void onFailure(HttpResponse response) {
                                super.onFailure(response);
                                Bitmap errorBitmap = bitmapPackage.request.getErrorBitmap();
                                Drawable errorDrawable = bitmapPackage.request.getErrorDrawable();
                                if (errorBitmap != null) {
                                    setImageToView(errorBitmap, bitmapPackage.request.getView());
                                } else if (errorDrawable != null) {
                                    setImageToView(errorDrawable, bitmapPackage.request.getView());
                                }
                                if (callback != null) {
                                    callback.onLoadFailed(response, response.httpRequest.getSession().getUrl());
                                }
                            }

                            @Override
                            public void onCancel(final HttpRequest request) {
                                super.onCancel(request);
                                UiHandler.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callback != null) {
                                            callback.onCancel(request.getSession().getUrl());
                                        }
                                    }
                                });
                            }
                        })
                        .build();

                WelikeHttp welikeHttp = bitmapPackage.config.getWelikeHttp();
                //将图片加载任务放入到Http请求队列,如果http请求那边有缓存的话,不会再去请求网络.
                welikeHttp.enqueue(request);


            } catch (InterruptedException e) {
                if (mQuit) {
                    break;
                }
            }


        }

    }

    /**
     * 将图片设置到View上
     *
     * @param bitmap
     * @param view
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN) @SuppressWarnings("deprecation")
	private void setImageToView(final Bitmap bitmap, final View view) {
        if (view != null) {
            if (view instanceof ImageView) {
                ((ImageView) view).setImageBitmap(bitmap);
            } else {
                if (Build.VERSION.SDK_INT > 15) {
                    view.setBackground(new BitmapDrawable(view.getResources(), bitmap));
                } else {
                    view.setBackgroundDrawable(new BitmapDrawable(view.getResources(), bitmap));
                }
            }
        }

    }

    /**
     * 将图片设置到View上
     *
     * @param drawable
     * @param view
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN) @SuppressWarnings("deprecation")
	private void setImageToView(final Drawable drawable, final View view) {
        if (view != null) {
            if (view instanceof ImageView) {
                ((ImageView) view).setImageDrawable(drawable);
            } else {
                if (Build.VERSION.SDK_INT > 15) {
                    view.setBackground(drawable);
                } else {
                    view.setBackgroundDrawable(drawable);
                }
            }
        }

    }

    /**
     * 将一次图片加载请求放入队列
     *
     * @param config
     * @param request
     */
    public void enqueue(BitmapConfig config, BitmapRequest request) {
        BitmapPackage bitmapPackage = new BitmapPackage(config, request);
        enqueue(bitmapPackage);
    }

    /**
     * 将一个需要加载的图片包裹放入队列
     *
     * @param bitmapPackage
     */
    public void enqueue(BitmapPackage bitmapPackage) {
        try {
            packageBlockingQueue.put(bitmapPackage);
        } catch (InterruptedException e) {
        }
    }

    /**
     * 取消所有还未处理的图片加载请求
     */
    public void cancelAll() {
        for (BitmapPackage bitmapPackage : packageBlockingQueue) {
            bitmapPackage.request.cancel();
        }
    }


    /**
     * 退出循环
     */
    public void quit() {
        mQuit = true;
    }

}
