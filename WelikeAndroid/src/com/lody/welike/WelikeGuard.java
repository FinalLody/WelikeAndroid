package com.lody.welike;

import android.os.Looper;
import android.widget.Toast;

import com.lody.welike.guard.Mode;
import com.lody.welike.guard.WelikeGuardThreadGroup;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>应用异常安全隔离机制</b>
 * <br>
 * 经过对<b>Android UI线程运行原理</b>进行深度剖析后的思考结晶.
 * <br>
 * 功能概述: 当在任何一个线程抛出任何未捕获的异常,都会被WelikeGuard拦截,
 * 在被拦截后,主线程不会卡死,仍然会继续运行下去.<b>这也许会改变我们的编程理念</b>.
 * <br>
 * <b>注意:</b> 目前我们的异常隔离机制是以方法追溯为单位的,也就是说,
 * 异常抛出所在的方法在异常抛出以后就不会继续执行下去.
 * <br>
 * <b>警告:</b> 本功能尚未成熟,使用本套应用异常隔离请确保您对本功能有所期待.
 *
 * @author Lody
 * @version 3.1
 */
public final class WelikeGuard {

    /**
     * 不需要捕获异常的类
     */
    public static List<Class<? extends Throwable>> NOT_NEED_CATCH_CLASSES = new ArrayList<>(3);
    public static List<Thread.UncaughtExceptionHandler> UNCAUGHT_HANDLER_LIST = new ArrayList<>(2);


    /**
     * 抛出的异常数的阀值,超过此阀值框架就会强制把app杀掉
     */
    public static int MAX_ERROR_COUNT = 10;

    /**
     * 异常隔离机制的模式
     */
    public static Mode guardMode = Mode.THROW_IF_UN_CATCH;

    /**
     * 开启异常安全隔离机制,<br>
     * <b>注意:</b>目前开启后无法关闭.
     */
    public static void enableGuard() {
        try {
            hookSystemThreadGroup();
        } catch (Throwable e) {
            //适应不同API版本的异常隔离机制
            hookSystemThreadGroup2();

        }
    }

    /**
     * 注册一个不需要捕获异常的类
     *
     * @param clazz
     */
    public static void registerUncatchClass(Class<? extends Throwable> clazz) {
        if (!NOT_NEED_CATCH_CLASSES.contains(clazz)) {
            NOT_NEED_CATCH_CLASSES.add(clazz);
        }
    }

    /**
     * 取消注册一个不需要捕获异常的类
     *
     * @param clazz
     */
    public static void unregisterUncatchClass(Class<? extends Throwable> clazz) {
        NOT_NEED_CATCH_CLASSES.remove(clazz);
    }

    /**
     * 注册一个未捕获监听器
     *
     * @param uncaughtExceptionHandler
     */
    public static void registerUnCaughtHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        if (!UNCAUGHT_HANDLER_LIST.contains(uncaughtExceptionHandler)) {
            UNCAUGHT_HANDLER_LIST.add(uncaughtExceptionHandler);
        }
    }

    /**
     * 取消注册一个未捕获监听器
     *
     * @param uncaughtExceptionHandler
     */
    public static void unregisterCaughtUandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        UNCAUGHT_HANDLER_LIST.remove(uncaughtExceptionHandler);
    }


    /**
     * 创建一个子线程,在子线程弹出一个Toast
     *
     * @param msg 显示的字符串
     */
    public static void newThreadToast(final String msg) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(WelikeContext.getApplication(), msg, Toast.LENGTH_SHORT).show();
                Looper.loop();

            }
        }).start();
    }


    private static void hookSystemThreadGroup() throws Throwable {
        //在Android 5.X上测试正常
        Field f_mMain = ThreadGroup.class.getDeclaredField("mainThreadGroup");
        Field f_mSystem = ThreadGroup.class.getDeclaredField("systemThreadGroup");
        f_mMain.setAccessible(true);
        f_mSystem.setAccessible(true);
        ThreadGroup mMain = (ThreadGroup) f_mMain.get(null);
        ThreadGroup mSystem = (ThreadGroup) f_mSystem.get(null);
        Field f_parent = ThreadGroup.class.getDeclaredField("parent");
        f_parent.setAccessible(true);
        f_parent.set(mMain, new WelikeGuardThreadGroup(mSystem));


    }

    private static void hookSystemThreadGroup2() {
        //在Android 5.x以下测试正常
        try {
            Field f_mMain = ThreadGroup.class.getDeclaredField("mMain");
            Field f_mSystem = ThreadGroup.class.getDeclaredField("mSystem");
            f_mMain.setAccessible(true);
            f_mSystem.setAccessible(true);
            ThreadGroup mMain = (ThreadGroup) f_mMain.get(null);
            ThreadGroup mSystem = (ThreadGroup) f_mSystem.get(null);
            Field f_parent = ThreadGroup.class.getDeclaredField("parent");
            f_parent.setAccessible(true);
            f_parent.set(mMain, new WelikeGuardThreadGroup(mSystem));
        } catch (Throwable e) {
        }
    }


}