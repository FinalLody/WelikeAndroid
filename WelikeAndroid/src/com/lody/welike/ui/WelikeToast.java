package com.lody.welike.ui;

import android.widget.Toast;

import com.lody.welike.WelikeContext;

/**
 * @author Lody
 * @version 1.0
 */
public class WelikeToast {

    /**
     * 直接显示一个Toast
     *
     * @param msg 内容
     */
    public static void toast(String msg) {
        makeToast(msg).show();
    }

    /**
     * 直接返回一个Toast
     *
     * @param msg 内容
     * @return
     */
    public static Toast makeToast(String msg) {
        return Toast.makeText(WelikeContext.getApplication(), msg, Toast.LENGTH_SHORT);
    }


}
