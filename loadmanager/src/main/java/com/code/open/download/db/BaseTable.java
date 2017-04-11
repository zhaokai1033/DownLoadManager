package com.code.open.download.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.code.open.download.Util;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库表基础类
 * Created by zhaokai on 2016-05-12
 * <p>
 * Email zhaokai1033@126.com
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class BaseTable<T> implements BaseColumns {

    private String mTableName;
    private SqlDataBase mSqLiteOpenHelper;

    public BaseTable(String tableName, SqlDataBase sqlHelper) {
        this.mTableName = tableName;
        this.mSqLiteOpenHelper = sqlHelper;
    }

    /**
     * 通过cursor转换为对象
     */
    public abstract T getItemFromCursor(Cursor cursor);

    /**
     * 创建 数据库 SQL exec cmd
     */
    public abstract String onBuildSqlCmdForCreate();

    /**
     * 对象转成contentValues
     */
    public abstract ContentValues getContentValues(T item);

    /**
     * 获得表名
     */
    public String getTableName() {
        return mTableName;
    }

    /**
     * 检索所有数据到列表中
     */
    public List<T> queryAll() {
        return queryByCase(null, null, null);
    }

    /**
     * 通过特定条件检索数据
     */
    public List<T> queryByCase(String where, String args[], String orderBy, String limit) {
        List<T> items = null;
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db == null)
                return null;
            items = new ArrayList<>();
            cursor = db.query(getTableName(), null, where, args, null, null, orderBy, limit);
            while (cursor.moveToNext()) {
                items.add(getItemFromCursor(cursor));
            }
        } catch (Exception e) {
            Util.d(getTableName(), e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return items;
    }

    public List<T> queryByCase(String where, String args[], String orderBy) {
        return queryByCase(where, args, orderBy, null);
    }

    /**
     * 获得数据行数
     */
    public int getCount(String where, String args[]) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db == null) {
                return 0;
            }

            cursor = db.query(getTableName(), null, where, args, null, null, null);
            return cursor.getCount();
        } catch (Exception e) {
            Util.d(getTableName(), e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    /**
     * 表是否已被创建
     *
     * @return true 已经在数据库中创建
     */
    public boolean tableIsExist() {
        boolean result = false;
        String tableName = getTableName();
        if (tableName == null) {
            return false;
        }
        Cursor cursor = null;
        String whereClause = "type ='table' and name ='" + tableName.trim() + "' ";
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db == null) {
                return false;
            }
            cursor = db.query("sqlite_master", null, whereClause, null, null, null, null);
            if (cursor.getCount() > 0) {
                result = true;
            }
        } catch (Exception e) {
            Util.d(getTableName(), e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 插入数据
     */
    public long insert(T item) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db == null) {
                return -1;
            }
            return db.insert(getTableName(), null, getContentValues(item));
        } catch (Exception e) {
            Util.d(getTableName(), e.toString());
        }
        return -1;
    }

    public long insert(ContentValues cv) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db == null)
                return -1;
            return db.insert(getTableName(), null, cv);
        } catch (Exception e) {
            Util.d(getTableName(), e.toString());
        }
        return -1;
    }

    /**
     * 通过特定条件删除数据
     */
    public int deleteByCase(String where, String args[]) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db == null)
                return -1;
            return db.delete(getTableName(), where, args);
        } catch (Exception e) {
            Util.d(getTableName(), e.toString());
        }
        return -1;
    }

    /**
     * 更新记录
     */
    public int updateByCase(T item, String where, String args[]) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db == null)
                return -1;

            ContentValues values = getContentValues(item);
            return db.update(getTableName(), values, where, args);
        } catch (Exception e) {
            Util.d(getTableName(), e.toString());
        }
        return -1;
    }

    /**
     * 更新记录
     */
    public int updateByCase(ContentValues cv, String where, String args[]) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db == null)
                return -1;

            return db.update(getTableName(), cv, where, args);
        } catch (Exception e) {
            Util.d(getTableName(), e.toString());
        }
        return -1;
    }

    /**
     * 通过ID查询
     */
    public T queryById(long id) {
        List<T> result = queryByCase(_ID + "=" + id, null, null);
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    /**
     * 获得String
     */
    @SuppressLint("UseValueOf")
    @SuppressWarnings({"hiding", "unchecked"})
    public <Q> Q getValue(Cursor cursor, String columnName, Class<Q> qClass) {
        int index = cursor.getColumnIndex(columnName);
        if (String.class.getName().equals(qClass.getName())) {
            if (index >= 0)
                return (Q) cursor.getString(index);
            return null;
        } else if (Integer.class.getName().equals(qClass.getName())) {
            if (index >= 0)
                return (Q) new Integer(cursor.getInt(index));
            return (Q) new Integer(0);
        } else if (Long.class.getName().equals(qClass.getName())) {
            if (index >= 0)
                return (Q) new Long(cursor.getLong(index));
            return (Q) new Long(0);
        } else if (Float.class.getName().equals(qClass.getName())) {
            if (index >= 0)
                return (Q) new Float(cursor.getFloat(index));
            return (Q) new Float(0);
        } else if (Double.class.getName().equals(qClass.getName())) {
            if (index >= 0)
                return (Q) new Double(cursor.getDouble(index));
            return (Q) new Double(0);
        } else if (Date.class.getName().equals(qClass.getName())) {
            if (index >= 0)
                return (Q) new Date(cursor.getLong(index));
            return (Q) new Date(System.currentTimeMillis());
        }
        return null;
    }

    /**
     * 获得可写的数据库
     *
     * @return SQLiteDatabase
     */
    public SQLiteDatabase getWritableDatabase() {
        if (mSqLiteOpenHelper != null)
            return mSqLiteOpenHelper.getWritableDatabase();
        return null;
    }

    /**
     * 获得只读的数据库
     *
     * @return SQLiteDatabase
     */
    public SQLiteDatabase getReadableDatabase() {
        if (mSqLiteOpenHelper != null)
            return mSqLiteOpenHelper.getReadableDatabase();
        return null;
    }

    /**
     * 获得数据库
     *
     * @return SqlDataBase
     */
    public SqlDataBase getSqlDataBase() {
        return mSqLiteOpenHelper;
    }


    /**
     * 给表增加一列
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public void addColumn(String column, String append) throws SQLException {
        SQLiteDatabase db = getWritableDatabase();
        if (db == null)
            return;
        try {
            db.execSQL("ALTER TABLE " + getTableName() + " DROP COLUMN " + column + ";");
        } catch (Exception e) {
            Util.d(getTableName(), "DROP COLUMN " + column + " ERROR , IGNORE IT.");
        }

        try {
            db.execSQL("ALTER TABLE " + getTableName() + " ADD COLUMN " + column + " " + append + ";");
        } catch (SQLException e) {
            Util.d(getTableName(), "Error when alter table " + getTableName() + " .");
            String strException = e.getMessage();
            if (strException.contains("duplicate column name")) {
                // 如果是这个异常信息，表示数据库中已经有这个字段了，这是正常的，不会对数据有异常行为
            } else {
                // throw e;
            }
        }
    }

    /**
     * 表被创建完后回调
     */
    public void onCreated(SqlDataBase db) {
        Util.d(getTableName(), getTableName() + "  Created!");
    }

    /**
     * 表被升级更新后回调
     */
    public void onUpgraded(SqlDataBase db, int oldDBVersion, int newDBVersion) {
        Util.d(getTableName(), getTableName() + " Upgraded!");
        if (!tableIsExist()) {
            Util.d(getTableName(), getTableName() + " is not Exist! After add a new table,please upgrade DataBase version!");
        }
    }

    /**
     * 获取表结构 字段名
     */
    public String[] getColumnNames() {
        Cursor cursor = getWritableDatabase().query(getTableName(), null, null, null, null, null, null);
        cursor.moveToFirst();
        String[] names = cursor.getColumnNames();
        cursor.close();
        return names;
    }
}
