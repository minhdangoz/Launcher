package com.klauncher.biddingos.commons.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;

/**
 * 数据库相关工具类
 */
public class DBUtils {
    /**
     * 安全关闭资源
     * @param resource 待关闭资源
     */
    public static void safeClose(SQLiteDatabase resource) {
        if (null != resource) {
            try {
                resource.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 安全关闭资源
     * @param resource 待关闭资源
     */
    public static void safeClose(Cursor resource) {
        if (null != resource) {
            try {
                resource.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将游标中的当前行数据，按照指定对象格式封装后返回
     * <br><br>
     *     传入的对象类必须存在public的默认构造器
     * @param clazz
     * @param cursor
     * @param <T>
     * @return
     */
    public static <T> T cursor2Obj(Class<T> clazz, Cursor cursor) {
        try {
            T obj = clazz.newInstance();
            for (String columnName : cursor.getColumnNames()) {
                Field f = null;
                try {
                    f = obj.getClass().getField(columnName);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                if (null == f) continue;
                f.setAccessible(true);

                Class<?> fieldClazz = f.getType();
                if (fieldClazz == int.class) {
                    f.setInt(obj, cursor.getInt(cursor.getColumnIndex(columnName)));
                } else if (fieldClazz == long.class) {
                    f.setLong(obj, cursor.getLong(cursor.getColumnIndex(columnName)));
                } else if (fieldClazz == float.class) {
                    f.setFloat(obj, cursor.getFloat(cursor.getColumnIndex(columnName)));
                } else if (fieldClazz == double.class) {
                    f.setDouble(obj, cursor.getDouble(cursor.getColumnIndex(columnName)));
                } else if (fieldClazz == short.class) {
                    f.setShort(obj, cursor.getShort(cursor.getColumnIndex(columnName)));
                } else if (fieldClazz == String.class) {
                    f.set(obj, cursor.getString(cursor.getColumnIndex(columnName)));
                }
            }
            return obj;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}
