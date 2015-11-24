package com.lody.welike;

/**
 * 有些人不太喜欢WelikeXXX.getDefault()这种方式,
 * 那么这个类适合你.<br>
 *
 * @author Lody
 * @version 1.0
 */
public class Welike {
    public static WelikeHttp http;
    public static WelikeBitmap bitmap;
    public static WelikeDao dao;

    static {
        http = WelikeHttp.getDefault();
        bitmap = WelikeBitmap.getDefault();
        dao = WelikeDao.instance();
    }

    /**
     * @see WelikeGuard#enableGuard()
     */
    public static void enableGuard() {
        WelikeGuard.enableGuard();
    }

    /**
     * @param name
     * @return
     * @see WelikeDao#instance(String)
     */
    public static WelikeDao getDao(String name) {
        return WelikeDao.instance(name);
    }

    /**
     * @param name
     * @param version
     * @return
     * @see WelikeDao#instance(String, int)
     */
    public static WelikeDao getDao(String name, int version) {
        return WelikeDao.instance(name);
    }
}
