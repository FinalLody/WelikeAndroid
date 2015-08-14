package com.lody.sample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.lody.sample.custom.CustomException;
import com.lody.welike.ui.WelikeActivity;
import com.lody.welike.ui.annotation.JoinView;

/**
 * @author Lody
 * @version 1.0
 */
public class GuardActivity extends WelikeActivity {

    @JoinView(id = R.id.throw_1)
    private Button throw1;
    @JoinView(id = R.id.throw_2)
    private Button throw2;
    @JoinView(id = R.id.throw_3)
    private Button throw3;
    @Override
    public void initGlobalView(Bundle savedInstanceState) {
        super.initGlobalView(savedInstanceState);
        setContentView(R.layout.guard_layout);
    }

    @Override
    public void onWidgetClick(View widget) throws CustomException {
        super.onWidgetClick(widget);
        if (widget == throw1){
            //点击会立刻退出
            throw new RuntimeException("我是一个RuntimeException.");
        }else if (widget == throw2){
            throw new Error("我是一个Error :)");

        }else if (widget == throw3){
            throwCustomException();
        }
    }


    /**
     * 抛出一个自定义异常
     */
    private void throwCustomException() {
        throw new CustomException();
    }
}
