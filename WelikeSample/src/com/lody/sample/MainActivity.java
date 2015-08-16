package com.lody.sample;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.lody.welike.ui.WelikeActivity;
import com.lody.welike.ui.annotation.JoinView;
import com.lody.welike.utils.MultiAsyncTask;

/**
 * @author Lody
 * @version 1.0
 */
public class MainActivity extends WelikeActivity {

    @JoinView(id = R.id.bitmao)
    private Button bitmapButton;
    @JoinView(id = R.id.db)
    private Button dbButton;
    @JoinView(id = R.id.http)
    private Button httpButton;
    @JoinView(id = R.id.guard)
    private Button guardButton;
    @Override
    public void initGlobalView(Bundle savedInstanceState) {
        super.initGlobalView(savedInstanceState);
        setContentView(R.layout.main_layout);

    }

    @Override
    public void initWidget() {
        super.initWidget();
    }

    @Override
    public void onWidgetClick(View widget) {
        super.onWidgetClick(widget);

        if (bitmapButton == widget){
            startActivity(new Intent(this,BitmapActivity.class));
        }else if (dbButton == widget){
            startActivity(new Intent(this,DbActivity.class));
        }else if (httpButton == widget){
            startActivity(new Intent(this,HttpActivity.class));
        }else if (guardButton == widget){
            startActivity(new Intent(this,GuardActivity.class));
        }

    }
}
