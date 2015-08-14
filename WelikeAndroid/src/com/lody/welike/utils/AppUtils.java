package com.lody.welike.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.lody.welike.WelikeContext;

/**
 * @author Lody
 *         <p/>
 *         针对Android SDK版本的工具类
 */
public class AppUtils {

    private static int APP_VERSION = 0;

    /**
     * 取得当前App版本号
     *
     * @return app版本, 获取不到则返回1.
     */
    public static int getAppVersion() {

        if (APP_VERSION == 0) {
            try {
                Context application = WelikeContext.getApplication();
                PackageInfo info = application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
                APP_VERSION = info.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                APP_VERSION = 1;
            }
        }

        return APP_VERSION;
    }
}
