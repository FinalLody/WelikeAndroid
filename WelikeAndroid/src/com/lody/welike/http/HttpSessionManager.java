package com.lody.welike.http;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Http会话的管理器
 *
 * @author lody
 * @version 1.2
 */
public class HttpSessionManager {

    /**
     * 内部维护一个Http会话管理器的单例
     */
    private static class HttpSessionManagerHolder {
        static HttpSessionManager INSTANCE = new HttpSessionManager();
    }

    /**
     * 取得会话管理器
     *
     * @return 会话管理器
     */
    public static HttpSessionManager getManager() {
        return HttpSessionManagerHolder.INSTANCE;
    }

    /*package*/ Map<String, HttpSession> urlToSessionMap = new ConcurrentHashMap<>(5);

    /**
     * 取出一个会话,会话不存在,则创建会话.
     *
     * @param url
     * @param requestMethod
     * @return
     */
    public HttpSession getSession(String url, RequestMethod requestMethod) {
        HttpSession session = urlToSessionMap.get(url);
        if (session == null) {
            synchronized (urlToSessionMap) {
                session = new HttpSession(url, requestMethod);
                urlToSessionMap.put(url, session);
            }
        }
        return session;
    }

    /**
     * 取得当前会话的数量
     *
     * @return
     */
    public int getSessionSize() {
        return urlToSessionMap.size();
    }

    /**
     * 结束一个会话
     *
     * @param session
     */
    public void finish(HttpSession session) {
        urlToSessionMap.remove(session.getUrl());
    }

    /**
     * 结束指定Url的会话
     *
     * @param url
     */
    public void finish(String url) {
        if (url != null) {
            urlToSessionMap.remove(url);
        }
    }


}
