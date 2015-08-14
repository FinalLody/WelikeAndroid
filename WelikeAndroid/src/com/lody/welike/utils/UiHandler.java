package com.lody.welike.utils;


import android.os.Handler;
import android.os.Looper;

/**
 * 封装了在<b>子线程时</b>到<b>主线程</b>运行一段逻辑的操作.
 *
 * @author Lody
 * @version 1.0
 */
public class UiHandler {

    private static Handler sUiHandler;


    /**
     * 在主线程运行一段逻辑
     *
     * @param runnable
     */
    public static void runOnUiThread(Runnable runnable) {
        initUIHandlerIfNeed();
        sUiHandler.post(runnable);
    }

    /**
     * 在主线程延时运行一段逻辑
     *
     * @param runnable
     * @param delayMills
     */
    public static void runOnUiThreadDelayed(Runnable runnable, long delayMills) {
        initUIHandlerIfNeed();
        sUiHandler.postDelayed(runnable, delayMills);
    }

    private static void initUIHandlerIfNeed() {
        if (sUiHandler == null) {
            synchronized (UiHandler.class) {
                if (sUiHandler == null) {
                    sUiHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
    }
}
