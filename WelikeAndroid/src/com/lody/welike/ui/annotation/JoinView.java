package com.lody.welike.ui.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link com.lody.welike.ui.WelikeActivity} 会将添加本注解的View自动绑定.
 * 免去了findViewByID以及强制转换的繁琐.
 *
 * @author Lody
 * @version 1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JoinView {
    int id() default 0;

    String name() default "";

    boolean click() default false;
}
