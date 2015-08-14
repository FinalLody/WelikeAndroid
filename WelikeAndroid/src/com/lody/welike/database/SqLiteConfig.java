package com.lody.welike.database;

import java.io.Serializable;

/**
 * @author Lody
 *         <p/>
 *         数据库配置信息
 */
public class SqLiteConfig implements Serializable {

//==============================================================
    //                          常量
    //==============================================================
    public static String DEFAULT_DB_NAME = "we_like.db";
    public static SqLiteConfig DEFAULT_CONFIG = new SqLiteConfig();
	private static final long serialVersionUID = -4069725570156436316L;
	
    //==============================================================
    //                          字段
    //==============================================================
    /**
     * 数据库名
     */
    private String dbName = DEFAULT_DB_NAME;

    /**
     * 是否为DEBUG模式
     */
    public boolean debugMode = true;

    /**
     * 数据库升级监听器
     */
    private DbUpdateListener dbUpdateListener;
    private String saveDir;
    private int dbVersion = 1;

    /**
     * 取得数据库的名称
     *
     * @return
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * 设置数据库的名称
     *
     * @param dbName
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * 设置数据库升级监听器
     *
     * @param dbUpdateListener
     */
    public void setDbUpdateListener(DbUpdateListener dbUpdateListener) {
        this.dbUpdateListener = dbUpdateListener;
    }

    /**
     * 取得数据库升级监听器
     *
     * @return
     */
    public DbUpdateListener getDbUpdateListener() {
        return dbUpdateListener;
    }

    /**
     * 取得数据库保存目录
     *
     * @return
     */
    public String getSaveDir() {
        return saveDir;
    }

    /**
     * 设置数据库的保存目录
     *
     * @param saveDir
     */
    public void setSaveDir(String saveDir) {
        this.saveDir = saveDir;
    }

    /**
     * 获取DB的版本号
     *
     * @return
     */
    public int getDbVersion() {
        return dbVersion;
    }

    /**
     * 设置DB的版本号
     *
     * @param dbVersion
     */
    public void setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
    }
}
