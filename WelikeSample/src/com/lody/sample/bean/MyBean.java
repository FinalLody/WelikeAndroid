package com.lody.sample.bean;

import com.lody.welike.WelikeDao;
import com.lody.welike.database.annotation.ID;
import com.lody.welike.database.annotation.Table;
import com.lody.welike.ui.WelikeToast;

/**
 * @author Lody
 * @version 1.0
 */
@Table(name = "MyTable", afterTableCreate = "afterTableCreate")
public class MyBean {

    //ID可有可无,如果你不需要id字段,可以不声明
    @ID
    public int id;
    public String name;
    public boolean isOK = true;

    /**
     * 在表被创建后回调的方法
     *
     * @param dao 数据库引擎
     */
    public static void afterTableCreate(WelikeDao dao) {

        WelikeToast.toast("MyBean对应的表被创建了!");

    }
}
