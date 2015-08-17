package com.lody.sample;

import android.app.Application;

import com.lody.welike.WelikeGuard;

/**
 * @author Lody
 * @version 1.0
 */
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        WelikeGuard.enableGuard();
        //此时只要抛出的不是RuntimeException,UI线程就会继续跑下去
        WelikeGuard.registerUnCaughtHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                WelikeGuard.newThreadToast("出现异常了: " + ex.getMessage() + " (" + ex.getClass().getSimpleName() + ")" );
            }
        });

    }
}
