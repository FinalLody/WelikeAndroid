package com.lody.welike;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lody.welike.database.DataType;
import com.lody.welike.database.DbUpdateListener;
import com.lody.welike.database.SQLMaker;
import com.lody.welike.database.SQLTypeParser;
import com.lody.welike.database.SqLiteConfig;
import com.lody.welike.database.TableBuilder;
import com.lody.welike.database.ValueConvertor;
import com.lody.welike.database.bean.TableInfo;
import com.lody.welike.reflect.Reflect;
import com.lody.welike.utils.WeLog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lody
 *         <p/>
 *         基于<b>DTO(DataToObject)</b>映射的数据库操纵模型.
 *         通过少量可选的注解,即可构造数据模型.
 *         增删查改异常轻松.
 * @version 1.3
 */
public class WelikeDao {

    /**
     * 缓存创建的数据库,以便防止数据库冲突.
     */
    /*package*/ static final Map<String, WelikeDao> DAO_MAP = new HashMap<>();

    /**
     * 数据库配置
     */
    private SqLiteConfig sqLiteConfig;
    /**
     * 内部操纵的数据库执行类
     */
    private SQLiteDatabase db;

    /**
     * 默认构造器
     *
     * @param config
     */
    private WelikeDao(SqLiteConfig config) {

        this.sqLiteConfig = config;
        String saveDir = config.getSaveDir();
        if (saveDir != null
                && saveDir.trim().length() > 0) {
            this.db = createDbFileOnSDCard(saveDir,
                    config.getDbName());
        } else {
            this.db = new SqLiteDbHelper(WelikeContext.getApplication().getApplicationContext()
                    .getApplicationContext(), config.getDbName(),
                    config.getDbVersion(), config.getDbUpdateListener())
                    .getWritableDatabase();
        }

    }

    /**
     * 根据配置取得用于操纵数据库的WeLikeDao实例
     *
     * @param config
     * @return
     */
    public static WelikeDao instance(SqLiteConfig config) {
        if (config.getDbName() == null) {
            throw new IllegalArgumentException("DBName is null in SqLiteConfig.");
        }
        WelikeDao dao = DAO_MAP.get(config.getDbName());
        if (dao == null) {
            dao = new WelikeDao(config);
            synchronized (DAO_MAP) {
                DAO_MAP.put(config.getDbName(), dao);
            }
        } else {//更换配置
            dao.applyConfig(config);
        }

        return dao;
    }

    /**
     * 根据默认配置取得操纵数据库的WeLikeDao实例
     *
     * @return
     */
    public static WelikeDao instance() {
        return instance(SqLiteConfig.DEFAULT_CONFIG);
    }

    /**
     * 取得操纵数据库的WeLikeDao实例
     *
     * @param dbName
     * @return
     */
    public static WelikeDao instance(String dbName) {
        SqLiteConfig config = new SqLiteConfig();
        config.setDbName(dbName);
        return instance(config);
    }

    /**
     * 取得操纵数据库的WeLikeDao实例
     *
     * @param dbVersion
     * @return
     */
    public static WelikeDao instance(int dbVersion) {
        SqLiteConfig config = new SqLiteConfig();
        config.setDbVersion(dbVersion);
        return instance(config);
    }

    /**
     * 取得操纵数据库的WeLikeDao实例
     *
     * @param listener
     * @return
     */
    public static WelikeDao instance(DbUpdateListener listener) {
        SqLiteConfig config = new SqLiteConfig();
        config.setDbUpdateListener(listener);
        return instance(config);
    }

    /**
     * 取得操纵数据库的WeLikeDao实例
     *
     * @param dbName
     * @param dbVersion
     * @return
     */
    public static WelikeDao instance(String dbName, int dbVersion) {
        SqLiteConfig config = new SqLiteConfig();
        config.setDbName(dbName);
        config.setDbVersion(dbVersion);
        return instance(config);
    }

    /**
     * 取得操纵数据库的WeLikeDao实例
     *
     * @param dbName
     * @param dbVersion
     * @param listener
     * @return
     */
    public static WelikeDao instance(String dbName, int dbVersion, DbUpdateListener listener) {
        SqLiteConfig config = new SqLiteConfig();
        config.setDbName(dbName);
        config.setDbVersion(dbVersion);
        config.setDbUpdateListener(listener);
        return instance(config);
    }

    /**
     * 配置为新的参数(不改变数据库名).
     *
     * @param config
     */
    private void applyConfig(SqLiteConfig config) {
        this.sqLiteConfig.debugMode = config.debugMode;
        this.sqLiteConfig.setDbUpdateListener(config.getDbUpdateListener());
    }

    public void release() {
        DAO_MAP.clear();
        if (sqLiteConfig.debugMode) {
            WeLog.d("缓存的DAO已经全部清除,将不占用内存.");
        }
    }


    /**
     * 在SD卡的指定目录上创建数据库文件
     *
     * @param sdcardPath sd卡路径
     * @param dbFileName 数据库文件名
     * @return
     */
    private SQLiteDatabase createDbFileOnSDCard(String sdcardPath,
                                                String dbFileName) {
        File dbFile = new File(sdcardPath, dbFileName);
        if (!dbFile.exists()) {
            try {
                if (dbFile.createNewFile()) {
                    return SQLiteDatabase.openOrCreateDatabase(dbFile, null);
                }
            } catch (IOException e) {
                throw new RuntimeException("无法在 " + dbFile.getAbsolutePath() + "创建DB文件.");
            }
        } else {
            //数据库文件已经存在,无需再次创建.
            return SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        }
        return null;
    }


    /**
     * 内部数据库监听器,负责派发接口.
     */
    private class SqLiteDbHelper extends SQLiteOpenHelper {

        private final DbUpdateListener dbUpdateListener;

        public SqLiteDbHelper(Context context, String name, int version,
                              DbUpdateListener dbUpdateListener) {
            super(context, name, null, version);
            this.dbUpdateListener = dbUpdateListener;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (sqLiteConfig.debugMode) {
                WeLog.d("数据库的onUpgrade调用.");
            }
            if (dbUpdateListener != null) {
                dbUpdateListener.onUpgrade(db, oldVersion, newVersion);
            } else { //干掉所有的表
                dropAllTable();
            }
        }

    }


    /**
     * 如果表不存在,需要创建它.
     *
     * @param clazz
     */
    private void createTableIfNeed(Class<?> clazz) {
        TableInfo tableInfo = TableBuilder.from(clazz);
        if (tableInfo.isCreate) {
            return;
        }
        if (!isTableExist(tableInfo)) {
            String sql = SQLMaker.createTable(tableInfo);
            if (sqLiteConfig.debugMode) {
                WeLog.w(sql);
            }
            db.execSQL(sql);
        }
    }

    /**
     * 判断表是否存在?
     *
     * @param table 需要盘的的表
     * @return
     */
    private boolean isTableExist(TableInfo table) {

        Cursor cursor = null;
        try {
            String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='"
                    + table.tableName + "' ";
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    return true;
                }
            }

        } catch (Throwable e) {

        } finally {
            if (cursor != null)
                cursor.close();
        }

        return false;
    }

    /**
     * 删除全部的表
     */
    public void dropAllTable() {

        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type ='table'", null);
        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                try {
                    dropTable(cursor.getString(0));
                } catch (SQLException e) {
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }

    }

    /**
     * 取得数据库中的表的数量
     *
     * @return 表的数量
     */
    public int tableCount() {
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type ='table'", null);
        int count = cursor == null ? 0 : cursor.getCount();
        if (cursor != null) {
            cursor.close();
        }
        return count;

    }


    /**
     * 取得数据库中的所有表名组成的List.
     *
     * @return
     */
    public List<String> getTableList() {
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type ='table'", null);
        List<String> tableList = new ArrayList<>();
        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                tableList.add(cursor.getString(0));
            }
        }

        return tableList;

    }

    /**
     * 删除一张表
     *
     * @param beanClass 表所对应的类
     */
    public void dropTable(Class<?> beanClass) {
        TableInfo tableInfo = TableBuilder.from(beanClass);
        dropTable(tableInfo.tableName);
        tableInfo.isCreate = false;
    }

    /**
     * 删除一张表
     *
     * @param tableName 表名
     */
    public void dropTable(String tableName) {
        String statement = "DROP TABLE IF EXISTS " + tableName;
        if (sqLiteConfig.debugMode) {
            WeLog.w(statement);
        }
        db.execSQL(statement);
        TableInfo tableInfo = TableBuilder.findTableInfoByName(tableName);
        if (tableInfo != null) {
            tableInfo.isCreate = false;
        }
    }


    /**
     * 存储一个Bean.
     *
     * @param bean
     * @return
     */
    public WelikeDao saveBean(Object bean) {
        createTableIfNeed(bean.getClass());
        if (bean != null) {
            String statement = SQLMaker.insertIntoTable(bean);
            if (sqLiteConfig.debugMode) {
                WeLog.w(statement);
            }
            db.execSQL(statement);
        }
        return this;

    }

    /**
     * 存储多个Bean.
     *
     * @param beans
     * @return
     */
    public WelikeDao saveBeans(Object[] beans) {
        for (Object o : beans) {
            saveBean(o);
        }

        return this;
    }

    /**
     * 存储多个Bean.
     *
     * @param beans
     * @return
     */
    public WelikeDao saveBeans(List<Object> beans) {

        for (Object o : beans) {
            saveBean(o);
        }

        return this;
    }

    /**
     * 寻找Bean对应的全部数据
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> findAll(Class<?> clazz) {
        createTableIfNeed(clazz);
        TableInfo tableInfo = TableBuilder.from(clazz);
        String statement = SQLMaker.selectTable(tableInfo.tableName);
        if (sqLiteConfig.debugMode) {
            WeLog.w(statement);
        }
        List<T> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(statement, null);
        while (cursor.moveToNext()) {

            T object = Reflect.on(clazz).create().get();

            if (tableInfo.containID) {
                DataType dataType = SQLTypeParser.getDataType(tableInfo.primaryField);
                String idFieldName = tableInfo.primaryField.getName();
                ValueConvertor.setKeyValue(cursor, object, tableInfo.primaryField, dataType, cursor.getColumnIndex(idFieldName));
            }

            for (Field field : tableInfo.fieldToDataTypeMap.keySet()) {
                DataType dataType = tableInfo.fieldToDataTypeMap.get(field);
                ValueConvertor.setKeyValue(cursor, object, field, dataType, cursor.getColumnIndex(field.getName()));
            }
            list.add(object);

        }
        cursor.close();


        return list;

    }

    /**
     * 根据where语句寻找Bean
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> findBeanByWhere(Class<?> clazz, String where) {
        createTableIfNeed(clazz);
        TableInfo tableInfo = TableBuilder.from(clazz);
        String statement = SQLMaker.findByWhere(tableInfo, where);
        if (sqLiteConfig.debugMode) {
            WeLog.w(statement);
        }
        List<T> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(statement, null);
        while (cursor.moveToNext()) {

            T object = Reflect.on(clazz).create().get();
            if (tableInfo.containID) {
                DataType dataType = SQLTypeParser.getDataType(tableInfo.primaryField);
                String idFieldName = tableInfo.primaryField.getName();
                ValueConvertor.setKeyValue(cursor, object, tableInfo.primaryField, dataType, cursor.getColumnIndex(idFieldName));
            }
            for (Field field : tableInfo.fieldToDataTypeMap.keySet()) {
                DataType dataType = tableInfo.fieldToDataTypeMap.get(field);
                ValueConvertor.setKeyValue(cursor, object, field, dataType, cursor.getColumnIndex(field.getName()));
            }//end for
            list.add(object);
        }//end while
        cursor.close();

        return list;

    }


    /**
     * 根据where语句删除Bean
     *
     * @param clazz
     * @return
     */
    public WelikeDao deleteBeanByWhere(Class<?> clazz, String where) {
        createTableIfNeed(clazz);
        TableInfo tableInfo = TableBuilder.from(clazz);
        String statement = SQLMaker.deleteByWhere(tableInfo, where);
        if (sqLiteConfig.debugMode) {
            WeLog.w(statement);
        }
        try {
            db.execSQL(statement);
        } catch (SQLException e) {
        }

        return this;
    }


    /**
     * 删除指定ID的bean
     *
     * @param tableClass
     * @param id
     * @return 删除的Bean
     */
    public WelikeDao deleteBeanByID(Class<?> tableClass, Object id) {
        createTableIfNeed(tableClass);
        TableInfo tableInfo = TableBuilder.from(tableClass);
        DataType dataType = SQLTypeParser.getDataType(id.getClass());
        if (dataType != null && tableInfo.primaryField != null) {
            //判断ID类型是否与数据类型匹配
            boolean match = SQLTypeParser.matchType(tableInfo.primaryField, dataType);
            if (!match) {//不匹配,抛出异常
                throw new IllegalArgumentException("类型 " + id.getClass().getName() + " 不是主键的类型,主键的类型应该为 " + tableInfo.primaryField.getType().getName());
            }
        }
        String idValue = ValueConvertor.valueToString(dataType, id);
        String statement = SQLMaker.deleteByWhere(tableInfo, tableInfo.primaryField == null ? "_id" : tableInfo.primaryField.getName() + " = " + idValue);
        if (sqLiteConfig.debugMode) {
            WeLog.w(statement);
        }

        try {
            db.execSQL(statement);
        } catch (SQLException e) {
            //删除失败
        }


        return this;

    }

    /**
     * 根据给定的where更新数据
     *
     * @param tableClass
     * @param where
     * @param bean
     * @return
     */
    public WelikeDao updateByWhere(Class<?> tableClass, String where, Object bean) {
        createTableIfNeed(tableClass);
        TableInfo tableInfo = TableBuilder.from(tableClass);
        String statement = SQLMaker.updateByWhere(tableInfo, bean, where);
        if (sqLiteConfig.debugMode) {
            WeLog.w(statement);
        }
        db.execSQL(statement);


        return this;
    }

    /**
     * 根据给定的id更新数据
     *
     * @param tableClass
     * @param id
     * @param bean
     * @return
     */
    public WelikeDao updateByID(Class<?> tableClass, Object id, Object bean) {
        createTableIfNeed(tableClass);
        TableInfo tableInfo = TableBuilder.from(tableClass);
        StringBuilder subStatement = new StringBuilder();
        if (tableInfo.containID) {
            subStatement.append(tableInfo.primaryField.getName()).append(" = ").append(ValueConvertor.valueToString(SQLTypeParser.getDataType(tableInfo.primaryField), id));
        } else {
            subStatement.append("_id = ").append((int) id);
        }
        updateByWhere(tableClass, subStatement.toString(), bean);

        return this;
    }

    /**
     * 根据ID查找Bean
     *
     * @param tableClass
     * @param id
     * @param <T>
     * @return
     */
    public <T> T findBeanByID(Class<?> tableClass, Object id) {
        createTableIfNeed(tableClass);
        TableInfo tableInfo = TableBuilder.from(tableClass);
        DataType dataType = SQLTypeParser.getDataType(id.getClass());
        if (dataType != null) {
            //判断ID类型是否与数据类型匹配
            boolean match = SQLTypeParser.matchType(tableInfo.primaryField, dataType) || tableInfo.primaryField == null;
            if (!match) {//不匹配,抛出异常
                throw new IllegalArgumentException("类型 " + id.getClass().getName() + " 不是主键的类型,主键的类型应该为 " + tableInfo.primaryField.getType().getName());
            }
            String idValue = ValueConvertor.valueToString(dataType, id);
            String statement = SQLMaker.findByWhere(tableInfo, tableInfo.primaryField == null ? "_id" : tableInfo.primaryField.getName() + " = " + idValue);
            if (sqLiteConfig.debugMode) {
                WeLog.w(statement);
            }
            Cursor cursor = db.rawQuery(statement, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                T bean = Reflect.on(tableClass).create().get();
                for (Field field : tableInfo.fieldToDataTypeMap.keySet()) {
                    DataType fieldType = tableInfo.fieldToDataTypeMap.get(field);
                    ValueConvertor.setKeyValue(cursor, bean, field, fieldType, cursor.getColumnIndex(field.getName()));
                }
                try {
                    Reflect.on(bean).set(tableInfo.containID ? tableInfo.primaryField.getName() : "_id", id);
                } catch (Throwable e) {
                    //我们允许Bean没有id字段,因此此异常可以忽略
                }
                cursor.close();
                return bean;
            }

        }
        return null;
    }

    /**
     * 通过VACUUM命令压缩数据库
     */
    public void vacuum() {
        db.execSQL("VACUUM");
    }

    /**
     * 调用本方法会释放当前数据库占用的内存,
     * 调用后请确保你不会在接下来的代码中继续用到本实例.
     */
    public void destroy() {
        DAO_MAP.remove(this);
        this.sqLiteConfig = null;
        this.db = null;
    }

    /**
     * 取得内部操纵的SqliteDatabase.
     *
     * @return
     */
    public SQLiteDatabase getDb() {
        return db;
    }

}
