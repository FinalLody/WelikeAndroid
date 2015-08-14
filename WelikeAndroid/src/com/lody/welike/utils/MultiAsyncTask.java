package com.lody.welike.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 重构版的AsyncTask,
 * 由于其在不同API版本有不同的表现,
 * 我们在这里采用在多API版本运行效果一致的异步任务执行器.
 *
 * @param <Param>  参数类型
 * @param <Update> 更新传递的参数类型
 * @param <Result> 结果类型
 * @author Lody
 */
public abstract class MultiAsyncTask<Param, Update, Result> {
    /**
     * 进度更新的标志
     */
    static final int MULTI_ASYNC_TASK_UPDATE = 0x001;
    /**
     * 任务完成的标志
     */
    static final int MULTI_ASYNC_TASK_RESULT = 0x002;
    /**
     * 用于子线程与主线程之间交互的Handler
     */
    private static HandlerPoster sHandlerPoster;
    /**
     * 用于执行一个异步任务, 如果想要修改并发数目,在构造器传入一个int表示并发数.
     */
    private static ThreadPoolExecutor MAIN_THREAD_POOL_EXECUTOR;

    public MultiAsyncTask() {
        this(5);
    }

    /**
     * @param count 并发数
     */
    public MultiAsyncTask(int count) {
        MAIN_THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(count, count, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    /**
     * 单例模式,取得用于主线程和子线程间交互的Handler
     *
     * @return 用于主线程和子线程间交互的Handler单例
     */
    private final HandlerPoster getPoster() {
        synchronized (MultiAsyncTask.class) {
            if (sHandlerPoster == null) {
                sHandlerPoster = new HandlerPoster();
            }
            return sHandlerPoster;
        }
    }

    /**
     * 调用本方法会触发 {@link #onTask(Object...)} 方法的调用, 你即可以在主线程调用本方法,
     * 也可以在子线程调用本方法.
     *
     * @param params 任务参数
     */
    @SafeVarargs
	public final void execute(Param... params) {
        onPrepare();
        TaskExecutor taskExecutor = new TaskExecutor(this, params);
        MAIN_THREAD_POOL_EXECUTOR.execute(taskExecutor);
    }

    /**
     * 直接在线程池执行一个任务
     *
     * @see #execute(Object[])
     */
    public final void execute(Runnable runnable) {
        MAIN_THREAD_POOL_EXECUTOR.execute(runnable);
    }

    /**
     * 在后台任务开始前调用
     */
    public void onPrepare() {
    }

    /**
     * 当你调用 {@link #execute(Object...)} 方法就会触发本方法回调.
     * 本方法执行在子线程.
     *
     * @param params 你在 {@link #execute(Object...)} 传入的参数
     * @return
     */
    public abstract Result onTask(@SuppressWarnings("unchecked") Param... params);

    /**
     * 当你调用 {@link #postUpdate(Object)},
     * 本方法就会回调(运行在主线程).
     *
     * @param update 更新值
     */
    public void onUpdate(Update update) {
    }

    /**
     * 任务执行完成后回调
     *
     * @param result 结果
     */
    public void onResult(Result result) {
    }

    /**
     * 更新进度值,将在{@link #onUpdate(Object)} 方法中被回调.
     *
     * @param update Specific update value
     */
    public final synchronized void postUpdate(Update update) {
        Message message = getPoster().obtainMessage(MULTI_ASYNC_TASK_UPDATE,
                new Messenger<>(this, update, null));
        message.sendToTarget();
    }


    /**
     * 用于主线程与子线程间交互
     *
     * @author Lody
     */
    private static class HandlerPoster extends Handler {
        public HandlerPoster() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            Messenger<?, ?, ?> result = (Messenger<?, ?, ?>) msg.obj;
            switch (msg.what) {
                case MultiAsyncTask.MULTI_ASYNC_TASK_UPDATE:
                    result.onUpdate();
                    break;
                case MultiAsyncTask.MULTI_ASYNC_TASK_RESULT:
                    result.onResult();
                    break;
            }
        }
    }

    /**
     * 用于保存任务结果.
     *
     * @param <Param>  参数类型
     * @param <Update> 进度类型
     * @param <Result> 结果类型
     * @author Lody
     */
    private static class Messenger<Param, Update, Result> {

        private MultiAsyncTask<Param, Update, Result> mAsyncTask;

        private Update mUpdate;

        private Result mResult;

        public Messenger(MultiAsyncTask<Param, Update, Result> asyncTask, Update update, Result result) {
            this.mAsyncTask = asyncTask;
            this.mUpdate = update;
            this.mResult = result;
        }

        public void onUpdate() {
            this.mAsyncTask.onUpdate(mUpdate);
        }

        public void onResult() {
            this.mAsyncTask.onResult(mResult);
        }

    }

    /**
     * 任务执行类
     *
     * @author Lody
     */
    private class TaskExecutor implements Runnable {

        /**
         * 保持参数
         */
        private Param[] mParams;

        /**
         * {@link MultiAsyncTask}实例
         */
        private MultiAsyncTask<Param, Update, Result> mTask;

        /**
         * 创建一个新线程执行任务,如果任务的数量超过了并发数,就会放到一个队列等待其他任务完成直到小于并发数.
         *
         * @param mTask
         * @param params
         */
        @SafeVarargs
		public TaskExecutor(MultiAsyncTask<Param, Update, Result> mTask, Param... params) {
            super();
            this.mTask = mTask;
            this.mParams = params;
        }

        @Override
        public void run() {
            postResult(mTask.onTask(mParams));
        }

        /**
         * 给主线程发生Result
         */
        private void postResult(Result result) {
            Message message = getPoster().obtainMessage(MULTI_ASYNC_TASK_RESULT,
                    new Messenger<>(mTask, null, result));
            message.sendToTarget();
        }
    }

}