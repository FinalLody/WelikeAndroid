package com.lody.welike.utils;

import android.util.Log;

import com.lody.welike.WelikeContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Lody
 *         Logcat工具类,能够控制Logcat的显示和关闭.
 *         同时不需要每次加上TAG,免去繁琐.
 */
public final class WeLog {

    private static String weTag = "We-Like";
    private static boolean openLog = true;


    private WeLog() {
    }


    /**
     * 设置Log标签
     *
     * @param tag
     * @return
     */
    public static void setTAG(String tag) {
        if (tag != null) {
            weTag = tag;
        }

    }

    /**
     * 打开Log
     *
     * @return
     */
    public static void openLog() {
        openLog = true;
    }

    /**
     * 关闭Log
     *
     * @return
     */
    public static void closeLog() {
        openLog = false;
    }

    /**
     * 打印一个info级别的消息
     *
     * @param msg
     * @return
     */
    public static void i(String msg) {
        if (openLog) {
            Log.i(weTag, msg);
        }
    }

    /**
     * 打印一个debug级别的消息
     *
     * @param msg
     * @return
     */
    public static void d(String msg) {
        if (openLog) {
            Log.d(weTag, msg);
        }
    }

    /**
     * 打印一个verbose级别的消息
     *
     * @param msg
     * @return
     */
    public static void v(String msg) {
        if (openLog) {
            Log.v(weTag, msg);
        }
    }


    /**
     * 打印一个berbose级别的消息
     *
     * @param msg
     * @return
     */
    public static void w(String msg) {
        if (openLog) {
            Log.w(weTag, msg);
        }
    }

    /**
     * 打印一个error级别的消息
     *
     * @param msg
     * @return
     */
    public static void e(String msg) {
        if (openLog) {
            Log.e(weTag, msg);
        }
    }

    /**
     * 打印一个info级别的消息,并换行
     *
     * @param msg
     * @return
     */
    public static void iNewLine(String msg) {
        if (openLog) {
            Log.i(weTag, msg + "\n");
        }
    }

    /**
     * 打印一个debug级别的消息,并换行
     *
     * @param msg
     * @return
     */
    public static void dNewLine(String msg) {
        if (openLog) {
            Log.d(weTag, msg + "\n");
        }
    }

    /**
     * 打印一个verbose级别的消息,并换行
     *
     * @param msg
     * @return
     */
    public static void vNewLine(String msg) {
        if (openLog) {
            Log.v(weTag, msg + "\n");
        }
    }

    /**
     * 打印一个verbose级别的消息,并换行
     *
     * @param msg
     * @return
     */
    public static void wNewLine(String msg) {
        if (openLog) {
            Log.w(weTag, msg + "\n");
        }
    }


    /**
     * 打印一个error级别的消息,并换行
     *
     * @param msg
     * @return
     */
    public static void eNewLine(String msg) {
        if (openLog) {
            Log.e(weTag, msg + "\n");
        }
    }


    /**
     * 对超过4000字的log进行分组处理.
     *
     * @param str
     */
    public static void il(String str) {
        int length = 3998;
        if (str.length() > length) {
            Log.i(weTag, "|_" + str.substring(0, length));
            il(str.substring(length));
        } else
            Log.i(weTag, "|_" + str);
    }


    /**
     * 读取当前应用的Logcat
     *
     * @return
     */
    public static String readLogCat() {
        StringBuilder log = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("logcat -d -v time " + WelikeContext.getApplication().getPackageName() + ":E *:S");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }
        } catch (IOException e) {
        }
        return log.toString();
    }


}
