package com.lody.sample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lody.sample.bean.MyBean;
import com.lody.welike.WelikeDao;
import com.lody.welike.ui.WelikeActivity;
import com.lody.welike.ui.WelikeToast;
import com.lody.welike.ui.annotation.JoinView;
import java.util.List;

/**
 * @author Lody
 * @version 1.0
 */
public class DbActivity extends WelikeActivity {

    @JoinView(id = R.id.add,click = true)
    Button addButton;
    @JoinView(id = R.id.remove,click = true)
    Button removeButton;
    @JoinView(id = R.id.query,click = true)
    Button queryButton;
    @JoinView(id = R.id.update,click = true)
    Button updateButton;
    @JoinView(name = "output")
    TextView textView;

    WelikeDao dao;


    @Override
    public void initRootView(Bundle savedInstanceState) {
        super.initRootView(savedInstanceState);
        dao = WelikeDao.instance("Welike.db");
        setContentView(R.layout.db_layout);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        refreshList();
    }

    @Override
    public void onWidgetClick(View widget) {
        super.onWidgetClick(widget);
        if (widget == addButton){
            addEvent();
        }else if (widget == removeButton){
            removeEvent();

        }else if (widget == queryButton){

            queryEvent();
        }else if (widget == updateButton){

            updateEvent();
        }
    }

    private void updateEvent() {
        try{
            MyBean myBean = new MyBean();
            myBean.isOK = false;
            myBean.name = "This is a updated field.";
            dao.updateByID(MyBean.class,1,myBean);
            WelikeToast.toast("更新成功!");
            refreshList();
        }catch (Throwable e){
            WelikeToast.toast("更新失败," + e.getMessage());
        }
    }

    private void queryEvent() {
        final EditText editText = new EditText(this);
        String queryField = "id=";
        editText.setText(queryField);
        editText.setSelection(queryField.length());

        new AlertDialog.Builder(this)
                .setTitle("输入where语句(查询条件)")
                .setNegativeButton("取消",null)
                .setView(editText).setPositiveButton("查询", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String where = editText.getText().toString();
                try {
                    List<MyBean> myBeans = dao.findBeanByWhere(MyBean.class, where);
                    textView.setText("");
                    for (MyBean myBean : myBeans){
                        textView.append("ID:      " + myBean.id );
                        textView.append("    Name :    " + myBean.name);
                        textView.append("    isOK :    " + myBean.isOK + "\n");
                    }
                } catch (Throwable e) {
                    WelikeToast.toast("操作失败:" + e.getMessage());
                    return;
                }
                WelikeToast.toast("操作完成!");
            }
        }).show();
    }

    private void removeEvent() {

        final EditText editText = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("输入要移除的Bean的ID")
                .setView(editText).setPositiveButton("移除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int id = Integer.valueOf(editText.getText().toString());
                try {
                    dao.deleteBeanByID(MyBean.class, id);
                } catch (Throwable e) {
                    WelikeToast.toast("操作失败:" + e.getMessage());
                    return;
                }
                refreshList();
                WelikeToast.toast("操作完成!");
            }
        }).show();
    }




    private void addEvent() {
        final EditText editText = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("输入要添加的内容")
                .setView(editText).setPositiveButton("添加", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyBean myBean = new MyBean();
                myBean.name = editText.getText().toString();
                dao.saveBean(myBean);
                refreshList();
            }
        }).setNegativeButton("取消",null).show();
    }

    private void refreshList() {
        textView.setText("");
        List<MyBean> myBeans = dao.findAll(MyBean.class);
        for (MyBean myBean : myBeans){
            textView.append("ID:      " + myBean.id );
            textView.append("    Name :    " + myBean.name);
            textView.append("    isOK :    " + myBean.isOK + "\n");
        }
    }
}
