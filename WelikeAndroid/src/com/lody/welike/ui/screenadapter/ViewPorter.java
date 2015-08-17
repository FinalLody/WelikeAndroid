package com.lody.welike.ui.screenadapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import com.lody.welike.utils.ScreenUtils;

/**
 * 屏幕适配大杀器
 *
 * @author Lody
 * @version 1.0
 */
public class ViewPorter {

    /**
     * 正在修改的视图
     */
    private View view;
    /**
     * 父视图,可能为null
     */
    private View parent;
    /**
     * 修改后宽度
     */
    private int toWidth;
    /**
     * 修改后的高度
     */
    private int toHeight;

    private ViewPorter(View view) {
        this.view = view;
        if (view == null) {
            throw new IllegalArgumentException("View == NULL!");
        }
    }

    /**
     * 根据传入的View构造一个ViewPorter.
     *
     * @param view
     * @return
     */
    public static ViewPorter from(View view) {
        return new ViewPorter(view);
    }

    public static ViewPorter from(View parent, int id) {
        return from(parent.findViewById(id));
    }

    public static ViewPorter from(Activity activity, int id) {
        return from(activity.findViewById(id));
    }


    /**
     * 以指定View或ViewGroup为父容器,父容器将用作屏幕适配的参照物.
     *
     * @param parent 服容器
     * @return
     */
    public ViewPorter of(View parent) {
        this.parent = parent;
        return this;
    }

    /**
     * 以整个屏幕为父容器
     *
     * @return
     */
    public ViewPorter ofScreen() {
        parent = null;
        return this;
    }

    /**
     * 以Activity的根视图为父容器
     *
     * @param activity
     * @return
     */
    public ViewPorter of(Activity activity) {

        this.parent = activity.getWindow().getDecorView();

        return this;
    }

    /**
     * view的宽度变为parent的divCount分之一,如果parent不存在,则为屏幕的divCount分之一.
     *
     * @param divCount
     * @return
     */
    public ViewPorter ofWidth(int divCount) {
        if (parent != null) {
            int width = parent.getWidth();
            toWidth = width / divCount;
        } else {
            toWidth = ScreenUtils.getScreenWidth(view.getContext()) / 2;
        }

        return this;
    }

    /**
     * view的宽度变为parent的divCount分之一,如果parent不存在,则为屏幕的divCount分之一.
     *
     * @param divCount
     * @return
     */
    public ViewPorter ofHeight(int divCount) {
        if (parent != null) {
            int height = parent.getHeight();
            toHeight = height / divCount;
        } else {
            toHeight = ScreenUtils.getScreenHeight(view.getContext()) / 2;
        }
        return this;
    }

    /**
     * 缩小宽度为父容器的divCount分之一,如果父容器不存在,缩小自身.
     *
     * @param divCount
     * @return
     */
    public ViewPorter divWidth(int divCount) {
        if (toWidth != 0) {
            toWidth /= divCount;
        } else {
            toWidth = ScreenUtils.getScreenWidth(view.getContext()) / divCount;
        }
        return this;
    }

    /**
     * 缩小高度为父容器的divCount分之一,如果父容器不存在,缩小自身.
     *
     * @param divCount
     * @return
     */
    public ViewPorter divHeight(int divCount) {
        if (toHeight != 0) {
            toHeight /= divCount;
        } else {
            toHeight = ScreenUtils.getScreenHeight(view.getContext()) / divCount;
        }
        return this;
    }

    public ViewPorter div(int divCount) {
        divWidth(divCount);
        divHeight(divCount);

        return this;
    }

    /**
     * 强制宽度为指定值
     *
     * @param toWidth 指定的宽度
     * @return
     */
    public ViewPorter castWidth(int toWidth) {
        this.toWidth = toWidth;
        return this;
    }

    /**
     * 强制宽度为指定值
     *
     * @param toHeight 指定的宽度
     * @return
     */
    public ViewPorter castHeight(int toHeight) {
        this.toHeight = toHeight;
        return this;
    }

    /**
     * 宽度铺满父容器
     *
     * @return
     */
    public ViewPorter fillWidth() {
        if (parent != null) {
            toWidth = parent.getWidth();
            return this;
        } else {
            View viewParent = (View) view.getParent();
            if (viewParent != null) {
                toWidth = viewParent.getWidth();
            } else {
                toWidth = ScreenUtils.getScreenWidth(view.getContext());
            }
        }

        return this;
    }

    /**
     * 高度铺满父容器
     *
     * @return
     */
    public ViewPorter fillHeight() {
        if (parent != null) {
            toWidth = parent.getWidth();
            return this;
        } else {
            View viewParent = (View) view.getParent();
            if (viewParent != null) {
                toWidth = viewParent.getHeight();
            } else {
                toWidth = ScreenUtils.getScreenHeight(view.getContext());
            }
        }

        return this;
    }

    /**
     * 宽度和高度铺满父容器
     *
     * @return
     */
    public ViewPorter fillWidthAndHeight() {
        fillWidth();
        fillHeight();
        return this;
    }

    /**
     * 改变view的透明度
     *
     * @param alpha
     * @return
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ViewPorter alpha(float alpha) {
        view.setAlpha(alpha);
        return this;
    }

    /**
     * 提交对View的修改.
     */
    public void commit() {
        ViewGroup.LayoutParams params = view.getLayoutParams();

        if (toWidth != 0) {
            params.width = toWidth;
        }
        if (toHeight != 0) {
            params.height = toHeight;
        }

        view.setLayoutParams(params);
        view.invalidate();

    }

    /**
     * 将宽度和高度设为与另一个View相同
     *
     * @param another
     */
    public ViewPorter sameAs(View another) {
        //toWidth = another.getLayoutParams().width;
        //toHeight = another.getLayoutParams().height;
        toWidth = another.getWidth();
        toHeight = another.getHeight();
        return this;
    }


}
