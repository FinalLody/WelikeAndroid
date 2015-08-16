package com.lody.welike.guard;

import com.lody.welike.guard.annotation.UnCatch;

/**
 * 继承本类的Throwable将不会被异常隔离机制拦截,直接抛出
 *
 * @author Lody
 * @version 1.0
 */
@UnCatch
public class UncaughtThrowable extends NullPointerException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 234876922020326875L;

}
