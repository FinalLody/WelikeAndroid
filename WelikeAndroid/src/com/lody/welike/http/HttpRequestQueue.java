package com.lody.welike.http;

import com.lody.welike.WelikeHttp;
import com.lody.welike.http.callback.HttpCallback;
import com.lody.welike.utils.UiHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Http请求的队列循环,负责Http请求的拉取.<br>
 * 一个{@link WelikeHttp}实例只有一个{@link HttpRequestQueue}.
 *
 * @author Lody
 */
public class HttpRequestQueue implements Runnable {

    /**
     * Http请求的队列,拉去不到会阻塞.
     */
    private BlockingQueue<HttpRequest> requestQueue = new LinkedBlockingDeque<>(/*MAX*/);

    /**
     * 是否已经退出
     */
    private boolean mQuit = false;

    /**
     * 向队列中添加一个Http请求
     *
     * @param request
     */
    public synchronized void enqueue(HttpRequest request) {
        try {
            requestQueue.put(request);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void run() {

        for (; ; ) {//while(true)的效率不如for(;;),话说,优化效率要从娃娃抓起...
            try {
                //如果队列中没有请求,就会阻塞,直到填充
                final HttpRequest request = requestQueue.take();
                //请求已经取消
                if (request.isCancel()) {
                    synchronized (request) {
                        final HttpCallback callback = request.getHttpCallback();
                        if (callback != null) {
                            //调用onCancel
                            runCancelOnUiThread(callback, request);
                        }
                        //结束会话
                        request.getSession().finish();
                    }
                    continue;
                }
                dispatchRequest(request);
                //会话到了这里就算接手完成了
                request.getSession().finish();

            } catch (InterruptedException e) {
                if (mQuit) {
                    break;
                }
            }
        }

    }

    /**
     * 在主线程执行onCancel回调
     *
     * @param callback Http回调
     * @param request  取消的Http请求
     */
    private void runCancelOnUiThread(final HttpCallback callback, final HttpRequest request) {
        UiHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback.onCancel(request);
            }
        });
    }

    /**
     * 派发Http请求
     *
     * @param httpRequest
     */
    private void dispatchRequest(final HttpRequest httpRequest) {
        UiHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HttpRequestExecutor.newExecutor(httpRequest).execute();
            }
        });

    }

    /**
     * 拉去队列最前端的一个Http请求.
     *
     * @return
     */
    public HttpRequest peekRequest() {
        return requestQueue.peek();
    }


    /**
     * 退出,队列不再继续循环.
     */
    public void quit() {
        synchronized (this) {
            cancelAll();
            mQuit = true;
            requestQueue.clear();
        }
        requestQueue = null;
    }

    /**
     * 取消队列中的所有任务
     */
    public void cancelAll() {
        synchronized (this) {
            for (HttpRequest request : requestQueue) {
                request.cancel();
            }
            requestQueue.clear();
        }
    }

}
