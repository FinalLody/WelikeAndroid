package com.lody.welike;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.lody.welike.reflect.Reflect;

import java.io.File;

/**
 * @author Lody
 *         <br>
 *         让你在任何地方轻松拿到Context.
 */
public final class WelikeContext {

    /**
     * Application实例
     */
    private static Application APP_INSTANCE;

    /**
     * 取得Application实例
     * (NOTE:必须在主线程调用!)
     *
     * @return
     */
    public static Application getApplication() {

        if (APP_INSTANCE == null) {
            synchronized (WelikeContext.class) {
                if (APP_INSTANCE == null) {
                    APP_INSTANCE = Reflect.on("android.app.ActivityThread").call("currentActivityThread").call("getApplication").get();
                }
            }
        }
        return APP_INSTANCE;
    }


    /**
     * 根据SD卡的挂载情况来选择缓存文件夹.
     * <b>(别忘了mkdir文件夹)</b>
     *
     * @param uniqueName
     * @return
     */
    public static File getDiskCacheDir(String uniqueName) {


        Context context = getApplication();
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }
}
