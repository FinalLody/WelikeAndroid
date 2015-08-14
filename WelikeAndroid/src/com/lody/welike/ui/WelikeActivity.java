package com.lody.welike.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.lody.welike.utils.MultiAsyncTask;

/**
 * 定义了一套回调标准的Activity
 *
 * @author Lody
 * @version 1.0
 */
public class WelikeActivity extends Activity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new MultiAsyncTask<Void, Void, Void>() {
            @Override
            public Void onTask(Void... _null) {
                //在子线程调用所有包含@initData的方法
                DynamicActivityBinder.invokeInitDataMethod(WelikeActivity.this);
                return null;
            }

            @Override
            public void onResult(Void _null) {
                super.onResult(_null);
                //数据加载完成
                onDataLoaded();
            }
        }.execute();
        initGlobalView(savedInstanceState);
        DynamicActivityBinder.joinAllView(this);
        initWidget();
    }

    /**
     * 数据加载完成后回调
     */
    public void onDataLoaded() {
    }

    /**
     * 加载根视图
     */
    public void initGlobalView(Bundle savedInstanceState) {
    }


    /**
     * 在这个回调上加载组件
     */
    public void initWidget() {
    }

    /**
     * 当一个Widget被点击时,本方法回调.
     *
     * @param widget
     */
    public void onWidgetClick(View widget) {
    }


    @Override
    public final void onClick(View v) {
        onWidgetClick(v);
    }
}
