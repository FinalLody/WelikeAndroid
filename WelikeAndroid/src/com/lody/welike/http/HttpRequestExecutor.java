package com.lody.welike.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.lody.welike.http.callback.HttpBitmapCallback;
import com.lody.welike.http.callback.HttpCallback;
import com.lody.welike.http.callback.HttpResultCallback;
import com.lody.welike.utils.DiskLruCache;
import com.lody.welike.utils.HashUtils;
import com.lody.welike.utils.IOUtils;
import com.lody.welike.utils.MultiAsyncTask;
import com.lody.welike.utils.UiHandler;
import com.lody.welike.utils.WeLog;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

/**
 * Http请求的执行器,每一个{@link HttpRequest}对应一个{@link HttpRequestBuilder}.
 * 任务托管于{@link MultiAsyncTask}的线程池.我们会控制并发量.
 *
 * @author Lody
 */
public class HttpRequestExecutor extends MultiAsyncTask<Void, Void, Void> {

    private HttpRequest request;
    private HttpCallback callback;
    private HttpResponse response;
    private boolean enableDiskLruCache;
    private boolean debugMode;

    public HttpRequestExecutor(HttpRequest request) {
        super(request.getHttpConfig().concurrency);
        this.request = request;

        debugMode = request.getHttpConfig().debugMode;
        this.callback = request.getHttpCallback();
        response = new HttpResponse();
        response.httpRequest = request;
        enableDiskLruCache = (request.getHttpConfig().getDiskLruCache() != null) && request.getHttpConfig().enableDiskCache;
    }


    @Override
    public void onPrepare() {
        super.onPrepare();
        if (callback != null) {
            callback.onPreRequest(request);
        }
    }

    @Override
    public Void onTask(Void... params) {

        if (request.isCancel()) {
            //任务已取消
            synchronized (request) {
                final HttpCallback callback = request.getHttpCallback();
                if (callback != null) {
                    //回调onCancel
                    UiHandler.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onCancel(request);
                        }
                    });
                }
            }
            return null;
        }

        //Note:不要保存任何DiskLruCache的引用,因为DiskLruCache可能被重新创建.
        String key = request.getCacheKey();
        String hashUrl = null;
        if (!(request.getParams().getUploadFiles().size() > 0) && enableDiskLruCache) {

            if (debugMode) WeLog.d("正在处理Http请求: " + key);

            hashUrl = HashUtils.hashKey(key);
            if (enableDiskLruCache) {

                if (debugMode) WeLog.d("请求的缓存为开启状态.");
                try {
                    DiskLruCache.Snapshot snapshot = request.getHttpConfig().getDiskLruCache().get(hashUrl);
                    if (snapshot != null) {
                        if (debugMode) WeLog.d("获取缓存快照成功!");
                        long timeoutDate = getTimeoutDate(snapshot, hashUrl);
                        if (timeoutDate != 0) {
                            long lostTime = (timeoutDate - System.currentTimeMillis());
                            if (lostTime > 0) {
                                if (debugMode)
                                    WeLog.d("距离缓存过期还有 " + (lostTime / 1000 / 60) + "分钟 " + (lostTime / 1000 - lostTime / 1000 / 60 * 60) + "秒");

                            } else {
                                if (debugMode) WeLog.d(key + "的缓存已过期.");

                            }
                        } else {
                            //缓存永久有效
                            if (debugMode) WeLog.d("发现一个永久有效的缓存");
                        }
                        if (timeoutDate == 0 || timeoutDate > System.currentTimeMillis()) {//没有过期
                            if (debugMode) WeLog.d("一个缓存命中!");

                            InputStream inputStream = snapshot.getInputStream(0);
                            if (inputStream != null) {
                                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                                HttpResponse oldResponse = (HttpResponse) objectInputStream.readObject();
                                if (oldResponse != null) {
                                    response.copyFrom(oldResponse);
                                }
                                callSuccessOnUiThread(callback, response);
                                callFinishOnUiThread(callback, response);
                                objectInputStream.close();
                                inputStream.close();

                                //有了缓存我们就不需要继续了.
                                return null;
                            }
                        }
                    }
                } catch (Throwable e) {
                }
            }
        }

        HttpCallback callback = request.getHttpCallback();
        try {
            HttpURLConnection connection = request.getSession().open(request);

            if (request.getSession().getRequestMethod() == RequestMethod.POST) {//如果是Post请求
                connection.setDoOutput(true);//Post请求必须打开Output

                String paramStatement = request.getParams().makeParams(request.getHttpConfig().getEncoding());
                if (paramStatement.length() > 1) {//包含参数
                    OutputStream outputStream = connection.getOutputStream();
                    if (outputStream != null) {
                        if (debugMode) WeLog.d("Post请求的参数为: " + paramStatement);

                        DataOutputStream os = new DataOutputStream(outputStream);
                        //将参数写入进去
                        request.writeToStream(os);
                    }
                }
            }
            response.contentLength = connection.getContentLength();
            response.responseCode = connection.getResponseCode();
            response.responseMessage = connection.getResponseMessage();
            response.copyHeader(connection.getHeaderFields());
            response.contentType = connection.getContentType();
            response.lastModifiedTime = connection.getLastModified();
            response.contentEncoding = connection.getContentEncoding();

            InputStream is = connection.getErrorStream();
            if (is != null) {
                response.errorMessage = new String(IOUtils.toByteArray(is));
                if (debugMode) {
                    WeLog.w("响应的ErrorMessage != NULL");
                }
            }

            is = connection.getInputStream();
            if (is != null) {
                response.data = IOUtils.toByteArray(is);
                if (debugMode && response.data != null) {
                    WeLog.w("响应的data != NULL");
                }
            }
            if (debugMode) WeLog.d("响应代码为:" + connection.getResponseCode());

            if (connection.getResponseCode() >= 300) {
                callFailureOnUiThread(callback, response);
            } else {
                if (enableDiskLruCache && hashUrl != null) {
                    DiskLruCache.Editor editor = request.getHttpConfig().getDiskLruCache().edit(hashUrl);
                    if (editor != null) {
                        if (debugMode) WeLog.d("开始写入缓存...");
                        //将请求写入LruDiskCache
                        saveResponse(editor);
                    }

                }
                callSuccessOnUiThread(callback, response);
            }

        } catch (IOException e) {
            if (response.errorMessage == null) {
                response.errorMessage = e.getMessage();
            }
            callFailureOnUiThread(callback, response);
        }


        return null;
    }

    /**
     * 将Http请求写入到缓存
     *
     * @param editor
     */
    private void saveResponse(DiskLruCache.Editor editor) {
        try {
            editor.set(1, String.valueOf(request.getHttpConfig().generateTimeoutDate()));
            OutputStream outputStream = editor.newOutputStream(0);
            if (outputStream != null) {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(response);
            } else {
                if (debugMode) WeLog.e("无法打开OutputStream T_T.");

            }
            editor.commit();
            request.getHttpConfig().getDiskLruCache().flush();
            //至此,缓存被成功写入.
            if (debugMode) WeLog.d("新的缓存已提交!");

        } catch (IOException e) {
            if (debugMode) WeLog.e("缓存写入失败,原因: " + e.getMessage());

            try {
                editor.abort();
                if (debugMode) WeLog.w("缓存写入已终止");

            } catch (IOException io) {
            }
        }
    }

    private void callFinishOnUiThread(final HttpCallback callback, final HttpResponse response) {
        UiHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onFinish(response);
                }
            }
        });
    }

    @Override
    public void onResult(Void aVoid) {
        super.onResult(aVoid);
        if (callback != null) {
            callback.onFinish(response);
        }
        this.request = null;
        this.callback = null;
        this.response = null;
    }

    /**
     * 在主线程回调onFailure
     *
     * @param callback
     * @param response
     */
    public void callFailureOnUiThread(final HttpCallback callback, final HttpResponse response) {

        UiHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onFailure(response);
                }
            }
        });
    }

    /**
     * 在主线程回调onSuccess
     *
     * @param callback
     * @param response
     */
    public void callSuccessOnUiThread(final HttpCallback callback, final HttpResponse response) {

        UiHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onSuccess(response);

                    if (callback instanceof HttpResultCallback) {
                        try {
                            ((HttpResultCallback) callback).onSuccess(new String(response.data, request.getHttpConfig().getEncoding()));
                        } catch (UnsupportedEncodingException e) {
                        }
                    } else if (callback instanceof HttpBitmapCallback) {
                        Bitmap bitmap = ((HttpBitmapCallback) callback).onProcessBitmap(response.data);
                        if (bitmap == null){
                            BitmapFactory.decodeByteArray(response.data, 0, response.data.length);
                        }

                        ((HttpBitmapCallback) callback).onSuccess(bitmap);
                    }
                }
            }
        });
    }

    /**
     * 取得缓存的过期时间
     *
     * @param snapshot 缓存快照
     * @param hashUrl  缓存对应的url键
     * @return
     */
    private long getTimeoutDate(DiskLruCache.Snapshot snapshot, String hashUrl) {
        if (enableDiskLruCache && hashUrl != null) {
            try {
                return Long.valueOf(snapshot.getString(1));
            } catch (Throwable e) {
            }
        }
        return -1;
    }


    /**
     * 新建一个Http请求执行器
     *
     * @param request
     * @return
     */
    public static HttpRequestExecutor newExecutor(HttpRequest request) {
        return new HttpRequestExecutor(request);
    }


}
