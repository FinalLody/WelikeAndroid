package com.lody.welike.guard;

import android.os.Looper;
import android.os.Process;

import com.lody.welike.WelikeGuard;
import com.lody.welike.guard.annotation.Catch;
import com.lody.welike.guard.annotation.UnCatch;
import com.lody.welike.reflect.NULL;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Welike安插的傀儡,异常隔离机制的底层守护类,
 * 我们将它植入到主线程.干扰原来的异常捕获机制:)
 *
 * @author Lody
 * @version 2.0
 */
public class WelikeGuardThreadGroup extends ThreadGroup {



    /**
     * 拦截的异常数量(允许高并发)
     */
    private static AtomicInteger errorHitCount = new AtomicInteger(0);

    public WelikeGuardThreadGroup(ThreadGroup mSystem) {
        super(mSystem, "system");
    }


    @Override
    public void uncaughtException(Thread t, Throwable e) {

        errorHitCount.incrementAndGet();
        //回调所有未捕获异常监听器
        for (Thread.UncaughtExceptionHandler uncaughtExceptionHandler : WelikeGuard.UNCAUGHT_HANDLER_LIST) {
            uncaughtExceptionHandler.uncaughtException(t, e);
        }
        if (WelikeGuard.NOT_NEED_CATCH_CLASSES.contains(e.getClass())) {
            super.uncaughtException(t, e);
            return;
        }
        if (e instanceof RuntimeException){
            killSelf();
        }else if (e instanceof UncaughtThrowable) {
            super.uncaughtException(t, e);
            return;
        }

        Catch catchAnnotation = e.getClass().getAnnotation(Catch.class);
        UnCatch unCatchAnnotation = e.getClass().getAnnotation(UnCatch.class);
        if (catchAnnotation != null && catchAnnotation.process().length() > 0) {
            String processMethodName = catchAnnotation.process();
            Class<?> processMethodClass = catchAnnotation.processClass();
            processThrowable(processMethodName, processMethodClass, e, t);
            if (WelikeGuard.guardMode == Mode.DONT_CATCH) {
                killSelf();
            }

        } else if (unCatchAnnotation != null && unCatchAnnotation.process().length() > 0) {
            String processMethodName = catchAnnotation.process();
            Class<?> processMethodClass = catchAnnotation.processClass();
            processThrowable(processMethodName, processMethodClass, e, t);
            if (WelikeGuard.guardMode != Mode.CATCH_ALL) {
                killSelf();
            }
        }
        killSelfIfNeed();

        //下面的代码很多人会难以理解,如果你无法理解,
        // 你可以尝试去掉while(true),然后看看异常隔离发生了什么变化.
        while (!killSelfIfNeed()) {

            try {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                Looper.loop();
            } catch (Throwable th) {
                errorHitCount.incrementAndGet();
                for (Thread.UncaughtExceptionHandler uncaughtExceptionHandler : WelikeGuard.UNCAUGHT_HANDLER_LIST) {
                    uncaughtExceptionHandler.uncaughtException(t, th);
                }
                if (e instanceof RuntimeException){
                    //如果是RuntimeException,我们不应该继续下去,应该退出,
                    killSelf();
                }
            }
        }

    }

    /**
     * 处理一个异常
     *
     * @param processMethodName  处理方法的名称(如:public static void process(Thread t,Throwable e));
     * @param processMethodClass 处理方法所在的类(可以为null)
     * @param e                  异常
     * @param t                  抛出异常的线程
     */
    private void processThrowable(String processMethodName, Class<?> processMethodClass, Throwable e, Thread t) {
        if (processMethodClass == NULL.class) {
            try {
                invokeProcessMethod(e.getClass(), processMethodName, t);
            } catch (Throwable throwable) {
            }
        } else {
            try {
                invokeProcessMethod(processMethodClass, processMethodName, t);

            } catch (Throwable throwable) {
            }
        }
    }

    private void invokeProcessMethod(Class<?> clazz, String processMethodName, Thread thread) throws Throwable {
        Method method = clazz.getDeclaredMethod(processMethodName, Thread.class);

        if (method != null && Modifier.isStatic(method.getModifiers())) {
            method.setAccessible(true);
            method.invoke(null, thread);
        }
    }

    /**
     * 看看是不是需要杀掉自己
     */
    public boolean killSelfIfNeed() {
        if (errorHitCount.get() >= WelikeGuard.MAX_ERROR_COUNT) {
            //杀掉自己,以防止死循环直至ANR出现
            killSelf();

            return true;
        }
        return false;
    }

    /**
     * 百分百杀掉自己
     */
    private void killSelf() {
        android.os.Process.killProcess(Process.myPid());
        System.exit(-1);
    }

    /**
     * 取得当前的异常数量
     *
     * @return 目前的异常数量
     */
    public static int getErrorCount() {
        return errorHitCount.get();
    }
}