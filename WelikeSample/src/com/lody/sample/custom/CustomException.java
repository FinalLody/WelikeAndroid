package com.lody.sample.custom;

import com.lody.welike.utils.WeLog;

/**
 *
 * 自定义的异常,当异常被抛出后,会自动回调onCatchThrowable函数.
 */
@com.lody.welike.guard.annotation.Catch(process = "onCatchThrowable")
public class CustomException extends IllegalAccessError {

	private static final long serialVersionUID = 4066053857828321133L;

	public static void onCatchThrowable(Thread t){
        WeLog.e("我被抛出了...");
    }
}
