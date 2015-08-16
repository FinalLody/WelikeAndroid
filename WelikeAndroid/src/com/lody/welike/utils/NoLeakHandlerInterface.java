package com.lody.welike.utils;

import android.os.Looper;
import android.os.Message;

/**
 * @author Lody
 *         实现此接口以替代Handler
 * @see NoLeakHandler
 * @see NoLeakHandler#NoLeakHandler(NoLeakHandlerInterface)
 * @see NoLeakHandler#NoLeakHandler(Looper, NoLeakHandlerInterface)
 * @see NoLeakHandler#innerHandler()
 */
public interface NoLeakHandlerInterface {
    boolean isValid();

    void handleMessage(Message msg);
}
