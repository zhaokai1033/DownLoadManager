package com.code.open.download.db;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * ================================================
 * Created by zhaokai on 2017/4/9.
 * Email zhaokai1033@126.com
 * Describe :
 * ================================================
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class DbHelper {

    private static DbHelper helper;
    private Map<String, SqlDataBase> mDatabases = new HashMap<>();
    private final String DEFAULT = "DefaultDb";

    private DbHelper(Context context) {
        if (context == null) {
            throw new RuntimeException("you should init it with ApplicationContext");
        }
        DefaultDb db = new DefaultDb(context.getApplicationContext());
        mDatabases.put(DEFAULT, db);
    }

    public static DbHelper getInstance(Context context) {
        if (helper == null) {
            synchronized (DbHelper.class) {
                helper = new DbHelper(context);
            }
        }
        return helper;
    }

    /**
     * 获取默认数据库的中表
     */
    public <T extends BaseTable<?>> T getTable(Class<T> clazz) {
        T table = mDatabases.get(DEFAULT).getTable(clazz);
        for (Map.Entry<String, SqlDataBase> entry : mDatabases.entrySet()) {
            table = getTable(entry.getValue(), clazz);
            if (table != null) {
                return table;
            }
        }
        return table;
    }

    /**
     * 获得表实例
     */
    public <T extends BaseTable<?>> T getTable(SqlDataBase db, Class<T> table) {
        if (db == null) {
            return null;
        }
        return db.getTable(table);
    }

    /**
     * 注册一个数据库
     *
     * @param db 用户自定义数据库
     */
    public void registerDB(SqlDataBase db) {
        if (db == null) {
            return;
        }
        mDatabases.put(db.getDatabaseName(), db);
    }

    /**
     * 解注册一个数据库
     *
     * @param db 用户自定义数据库
     */
    public void closeDb(SqlDataBase db) {
        if (db == null) {
            return;
        }
        closeDb(db.getDatabaseName());
    }

    /**
     * 解注册一个数据库
     *
     * @param dbName 数据库名称
     */
    public void closeDb(String dbName) {
        SqlDataBase db = getDb(dbName);
        if (db != null) {
            db.close();
            mDatabases.remove(dbName);
        }
    }

    /**
     * 返回一个数据库对象
     *
     * @param dbName 数据库名
     */
    public SqlDataBase getDb(String dbName) {
        if (TextUtils.isEmpty(dbName)) {
            return null;
        }
        return mDatabases.get(dbName);
    }
}
