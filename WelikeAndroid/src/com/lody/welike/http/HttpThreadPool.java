package com.lody.welike.http;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 优化的线程池,线程池的参数来自{@link android.os.AsyncTask}.
 *
 * @author Lody
 */
public class HttpThreadPool {

    private HttpThreadPool() {

    }

    // 线程池核心线程数
    private static int CORE_POOL_SIZE = 5;

    // 线程池最大线程数
    private static int MAX_POOL_SIZE = 100;

    // 额外线程空状态生存时间
    private static int KEEP_ALIVE_TIME = 10 * 1000;

    // 阻塞队列。当核心线程都被占用，且阻塞队列已满的情况下，才会开启额外线程。
    private static BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(
            10);

    // 线程工厂
    private static ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicInteger integer = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "WL-Thread #"
                    + integer.getAndIncrement());
        }
    };

    // 创建线程池
    private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
            KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue, threadFactory);
    ;

    /**
     * 从线程池中抽取线程，执行指定的Runnable对象
     *
     * @param runnable
     */
    public static void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }

    /**
     * 取得执行器
     *
     * @return
     */
    public static ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPool;
    }

}