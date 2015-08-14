package com.lody.welike.database;

import com.lody.welike.database.annotation.Ignore;
import com.lody.welike.database.annotation.NotNull;

import java.lang.reflect.Field;

/**
 * @author Lody
 *         解析Java的<b>数据类型</b>,将其转换为对应的SQL类型.
 * @version 2.1
 */
public class SQLTypeParser {

    /**
     * 根据字段类型匹配它在数据库中的对应类型.
     *
     * @param field
     * @return
     */
    public static DataType getDataType(Field field) {
        Class<?> clazz = field.getType();
        if (clazz == (String.class)) {
            return DataType.TEXT.nullable((field.getAnnotation(NotNull.class) == null));
        } else if (clazz == (int.class) || clazz == (Integer.class)) {
            return DataType.INTEGER.nullable((field.getAnnotation(NotNull.class) == null));
        } else if (clazz == (float.class) || clazz == (Float.class)) {
            return DataType.FLOAT.nullable((field.getAnnotation(NotNull.class) == null));
        } else if (clazz == (long.class) || clazz == (Long.class)) {
            return DataType.BIGINT.nullable((field.getAnnotation(NotNull.class) == null));
        } else if (clazz == (double.class) || clazz == (Double.class)) {
            return DataType.DOUBLE.nullable((field.getAnnotation(NotNull.class) == null));
        }
        return null;
    }

    /**
     * 根据字段类型匹配它在数据库中的对应类型.
     *
     * @param clazz
     * @return
     */
    public static DataType getDataType(Class<?> clazz) {
        if (clazz == (String.class)) {
            return DataType.TEXT;
        } else if (clazz == (int.class) || clazz == (Integer.class)) {
            return DataType.INTEGER;
        } else if (clazz == (float.class) || clazz == (Float.class)) {
            return DataType.FLOAT;
        } else if (clazz == (long.class) || clazz == (Long.class)) {
            return DataType.BIGINT;
        } else if (clazz == (double.class) || clazz == (Double.class)) {
            return DataType.DOUBLE;
        }
        return null;
    }

    /**
     * 字段类型与数据类型是否匹配?
     *
     * @param field
     * @param dataType
     * @return
     */
    public static boolean matchType(Field field, DataType dataType) {
        DataType fieldDataType = getDataType(field.getType());
        return dataType != null && fieldDataType == (dataType);
    }

    /**
     * 字段是否可以被数据库忽略?
     *
     * @param field
     * @return
     */
    public static boolean isIgnore(Field field) {
        return field.getAnnotation(Ignore.class) != null;
    }


}
