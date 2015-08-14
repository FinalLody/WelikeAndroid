package com.lody.sample.bean;

import com.lody.welike.database.annotation.ID;
import com.lody.welike.database.annotation.Table;

/**
 * @author Lody
 * @version 1.0
 */
@Table(name = "MyTable")
public class MyBean {
    //ID可有可无,如果你不需要id字段,可以不声明
    @ID
    public int id;
    public String name;
}
