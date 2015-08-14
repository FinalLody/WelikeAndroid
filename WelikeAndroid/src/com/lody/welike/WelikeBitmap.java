package com.lody.welike;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import com.lody.welike.bitmap.AsyncBitmapLoader;
import com.lody.welike.bitmap.BitmapConfig;
import com.lody.welike.bitmap.BitmapPreprocessor;
import com.lody.welike.bitmap.BitmapRequest;
import com.lody.welike.bitmap.callback.BitmapCallback;
import com.lody.welike.http.HttpThreadPool;

import java.io.File;

/**
 * 图片加载引擎的核心类,
 * 拥有完善的异步回调机制和缓存机制.
 * 我们使用{@link WelikeHttp}做为图片的磁盘缓存器和Http请求器.
 * 良好的复用机制能够尽可能的避免OOM的产生.
 *
 * @author Lody
 * @version 1.0
 */
public class WelikeBitmap {

    /**
     * 保持WeLikeBitmap的默认实例.
     */
    private static WelikeBitmap INSTANCE;

    /**
     * Bitmap加载引擎的配置
     */
    private BitmapConfig bitmapConfig;

    /**
     * 图片的加载器
     */
    private AsyncBitmapLoader bitmapLoader;

    /**
     * 默认构造器,构建一个WelikeBitmap,
     * 我们更建议使用全局的{@link WelikeBitmap#getDefault()}单例.
     */
    public WelikeBitmap() {
        this(new BitmapConfig());
    }


    /**
     * 根据配置创建一个WelikeBitmap实例
     *
     * @param config 图片加载引擎的配置
     */
    public WelikeBitmap(BitmapConfig config) {
        this.bitmapConfig = config;
        bitmapLoader = new AsyncBitmapLoader();
        HttpThreadPool.execute(bitmapLoader);
    }

    public void applyConfig(BitmapConfig config) {
        this.bitmapConfig.applyConfig(config);
    }

    /**
     * @return Bitmap加载引擎的配置
     */
    public BitmapConfig getBitmapConfig() {
        return bitmapConfig;
    }

    /**
     * 取得WelikeBitmap的全局单例,我们推荐使用本方法.
     *
     * @return 全局的WelikeBitmap单例
     */
    public static WelikeBitmap getDefault() {
        if (INSTANCE == null) {
            synchronized (WelikeBitmap.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WelikeBitmap();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 异步显示一张图片
     *
     * @param view 要显示图片的View
     * @param url  图片的Url
     */
    public BitmapRequest loadBitmap(View view, String url) {
        return loadBitmap(view, url, null);
    }


    /**
     * 异步显示一张图片
     *
     * @param view     要显示图片的View
     * @param url      图片的Url
     * @param callback 回调
     */
    public BitmapRequest loadBitmap(View view, String url, BitmapCallback callback) {
        return loadBitmap(view, url, 0, 0, callback);
    }


    /**
     * 异步加载一张图片
     *
     * @param view          要显示图片的View
     * @param url           图片的Url
     * @param loadingBitmap 加载中显示的图片
     * @param callback      图片加载回调
     */
    public BitmapRequest loadBitmap(View view, String url, Bitmap loadingBitmap, BitmapCallback callback) {
        return loadBitmap(view, url, loadingBitmap, null, callback);
    }


    /**
     * 异步加载一张图片
     *
     * @param url           图片的Url
     * @param view          要显示的图片
     * @param loadingBitmap 加载中显示的图片
     * @param errorBitmap   加载错误时显示的图片
     * @param callback      图片加载回调
     */
    public BitmapRequest loadBitmap(View view, String url, Bitmap loadingBitmap, Bitmap errorBitmap, BitmapCallback callback) {
        return loadBitmap(view, url, 0, 0, loadingBitmap, errorBitmap, callback);
    }

    /**
     * 异步加载一张图片
     *
     * @param url           图片的Url
     * @param view          要显示的图片
     * @param loadingBitmap 加载中显示的图片
     * @param errorBitmap   加载错误时显示的图片
     */
    public BitmapRequest loadBitmap(View view, String url, Bitmap loadingBitmap, Bitmap errorBitmap) {
        return loadBitmap(view, url, 0, 0, loadingBitmap, errorBitmap, null);
    }


    /**
     * 异步显示一张图片,图片会缩放为所设的宽度和高度.
     * 当宽度和高度为0时,我们将使用图片的原高.
     *
     * @param view          需要加载图片的View
     * @param url           图片Url
     * @param width         图片缩放后的宽度
     * @param height        图片缩放后的高度
     * @param loadingBitmap 加载中显示的图片
     * @param errorBitmap   加载错误时显示的图片
     */
    public BitmapRequest loadBitmap(View view, String url, int width, int height, Bitmap loadingBitmap, Bitmap errorBitmap) {

        return loadBitmap(view, url, width, height, loadingBitmap, errorBitmap, null);

    }

    /**
     * 异步显示一张图片,图片会缩放为所设的宽度和高度.
     * 当宽度和高度为0时,我们将使用图片的原高.
     *
     * @param view          需要加载图片的View
     * @param url           图片Url
     * @param width         图片缩放后的宽度
     * @param height        图片缩放后的高度
     * @param loadingBitmap 加载中显示的图片
     * @param errorBitmap   加载错误时显示的图片
     * @param callback      加载回调
     */
    public BitmapRequest loadBitmap(View view, String url, int width, int height, Bitmap loadingBitmap, Bitmap errorBitmap, BitmapCallback callback) {
        BitmapRequest request = new BitmapRequest(view, url, width, height, loadingBitmap, errorBitmap, callback);
        BitmapConfig config = bitmapConfig;
        bitmapLoader.enqueue(config, request);

        return request;

    }

    /**
     * 异步显示一张图片,图片会缩放为所设的宽度和高度.
     * 当宽度和高度为0时,我们将使用图片的原高.
     *
     * @param view            需要加载图片的View
     * @param url             图片Url
     * @param width           图片缩放后的宽度
     * @param height          图片缩放后的高度
     * @param loadingDrawable 加载中显示的图片
     * @param errorDrawable   加载错误时显示的图片
     * @param callback        加载回调
     */
    public BitmapRequest loadBitmap(View view, String url, int width, int height, Drawable loadingDrawable, Drawable errorDrawable, BitmapCallback callback) {
        BitmapRequest request = new BitmapRequest(view, url, width, height, loadingDrawable, errorDrawable, callback);
        BitmapConfig config = bitmapConfig;
        bitmapLoader.enqueue(config, request);

        return request;

    }

    /**
     * 异步加载一张图片
     *
     * @param view            要显示的图片
     * @param url             图片的Url
     * @param width           宽度
     * @param height          高度
     * @param loadingDrawable 加载中显示的图片的R资源
     * @param errorDrawable   错误时的图片的R资源
     * @param callback        图片加载的回调
     * @return 图片加载对应的回调
     */
    @SuppressLint("NewApi") @SuppressWarnings("deprecation")
	public BitmapRequest loadBitmap(View view, String url, int width, int height, int loadingDrawable, int errorDrawable, BitmapCallback callback) {
        Context context = WelikeContext.getApplication();
        Drawable loading = null;
        if (loadingDrawable != 0) {
            if (Build.VERSION.SDK_INT > 21) {
                loading = context.getDrawable(loadingDrawable);
            } else {
                loading = context.getResources().getDrawable(loadingDrawable);
            }
        }
        Drawable error = null;
        if (errorDrawable != 0) {
            if (Build.VERSION.SDK_INT > 21) {
                error = context.getDrawable(errorDrawable);
            } else {
                error = context.getResources().getDrawable(errorDrawable);
            }
        }
        return loadBitmap(view, url, width, height, loading, error, callback);
    }

    /**
     * 异步加载一张图片
     *
     * @param view            要显示的图片
     * @param url             图片的Url
     * @param loadingDrawable 加载中显示的图片的R资源
     * @param errorDrawable   错误时的图片的R资源
     * @param callback        图片加载的回调
     * @return 图片加载对应的回调
     */
    public BitmapRequest loadBitmap(View view, String url, int loadingDrawable, int errorDrawable, BitmapCallback callback) {

        return loadBitmap(view, url, 0, 0, loadingDrawable, errorDrawable, callback);
    }

    /**
     * 异步加载一张图片
     *
     * @param view            要显示的图片
     * @param url             图片的Url
     * @param loadingDrawable 加载中的图片的R资源
     * @param errorDrawable   错误时的图片的R资源
     * @return 图片加载对应的请求
     */
    public BitmapRequest loadBitmap(View view, String url, int loadingDrawable, int errorDrawable) {

        return loadBitmap(view, url, 0, 0, loadingDrawable, errorDrawable, null);
    }

    /**
     * 将一个请求送至图片加载器的队列中
     *
     * @param config
     * @param request
     */
    public void enqueue(BitmapConfig config, BitmapRequest request) {
        bitmapLoader.enqueue(config, request);
    }


    /**
     * 从本地加载一张图片
     *
     * @param imageView
     * @param imageFile
     */
    public void loadBitmap(ImageView imageView, File imageFile) {
        Bitmap bitmap = getBitmapConfig().getMemoryLruCache().getBitmap(imageFile.getAbsolutePath());
        if (bitmap == null) {
            bitmap = BitmapPreprocessor.decodeFileNoOOM(imageFile);
            getBitmapConfig().getMemoryLruCache().putBitmap(imageFile.getAbsolutePath(), bitmap);
        }
        imageView.setImageBitmap(bitmap);
    }


    /**
     * 取消所有的图片加载请求
     */
    public void cancelAll() {
        bitmapLoader.cancelAll();
    }

    /**
     * 与{@link WelikeHttp#destroy()}同理.
     */
    public void destory() {
        getBitmapConfig().getWelikeHttp().destroy();
        getBitmapConfig().getMemoryLruCache().clearAllBitmap();
        bitmapLoader.quit();
    }

    /**
     * 清除磁盘缓存
     */
    public void clearCache() {
        getBitmapConfig().getWelikeHttp().clearCache();
    }
}
