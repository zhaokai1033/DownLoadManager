package com.code.open.download.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.code.open.download.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by zhaokai on 2016-05-12
 * <p>
 * Email zhaokai1033@126.com
 * 数据库基类
 */
@SuppressWarnings({"WeakerAccess", "unused"})
abstract public class SqlDataBase extends SQLiteOpenHelper {
    public static final String TAG = "SqlDataBase";
    // 添加mDefaultWritableDatabase防止无限递归
    private SQLiteDatabase mDefaultWritableDatabase = null;
    private Hashtable<Class<? extends BaseTable<?>>, BaseTable<?>> mDbTables = new Hashtable<>();
    protected Context mContext;

    public SqlDataBase(Context context, String name, SQLiteDatabase.CursorFactory factory,
                       int version) {
        super(context, name, null, version);
        mContext = context;
        onRegisterTables();
    }

//    /**
//     * 创建 数据库 SQL exec cmd
//     * @return
//     */
//    protected abstract List<String> buildSqlForCreate();

    /**
     * 注册数据库中所有的表,必须调用registerTable进行注册
     */
    protected abstract void onRegisterTables();

    /**
     * 注册数据库中的表
     */
    protected void registerTable(Class<? extends BaseTable<?>> table,
                                 BaseTable<?> baseTable) {
        if (table == null || baseTable == null) {
            Util.d(TAG,
                    "registerTable() --- table == null || baseTable == null");
            return;
        }
        BaseTable<?> tmpBaseTable = getTable(table);
        if (tmpBaseTable == null) {
            mDbTables.put(table, baseTable);
        }
    }

    /**
     * 获得表实例
     *
     * @param table 表文件
     * @return T extends BaseTable
     */
    @SuppressWarnings({"unchecked"})
    public <T extends BaseTable<?>> T getTable(Class<T> table) {
        if (mCheckDatabaseAtAsserting) {
            Util.d(TAG, "getTable() --- mCheckDatabaseAtAsserting is true");
            return null;
        }
        return (T) mDbTables.get(table);
    }

    /**
     * 表是否已经被创建
     *
     * @return true 已经被创建
     */
    public <T extends BaseTable<?>> boolean tableHasCreated(Class<T> table) {
        BaseTable<?> myTable = getTable(table);
        return myTable != null && myTable.tableIsExist();
    }

    /**
     * 数据库被创建时调用
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建数据库表
        this.mDefaultWritableDatabase = db;
        createAllTable(db);
        //createAllTable(mDefaultWritableDatabase);
    }

    /**
     * 数据库升级时调用
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Util.d(TAG, "+++SqlDataBase,fromVersion:" + oldVersion
                + ",toVersion:" + newVersion);
        this.mDefaultWritableDatabase = db;
        if (mDbTables.size() > 0) {
            for (BaseTable<?> baseTable : mDbTables.values()) {
                if (!baseTable.tableIsExist()) {
                    createTable(baseTable, db);
                } else {
                    baseTable.onUpgraded(this, oldVersion, newVersion);
                }
            }
        }
    }

    /**
     * 创建所有数据库表
     */
    protected void createAllTable(SQLiteDatabase db) {
        if (mCheckDatabaseAtAsserting) {
            Util.d(TAG,
                    "createAllTable() --- mCheckDatabaseAtAsserting is true");
            return;
        }

        if (mDbTables.size() > 0) {
            for (BaseTable<?> baseTable : mDbTables.values()) {
                createTable(baseTable, db);
            }
        }
    }

    // 在数据库中创建一个新表
    private void createTable(BaseTable<?> baseTable, SQLiteDatabase db) {
        if (baseTable == null || db == null) {
            Util.d(TAG, "createTable() --- baseTable == null || db == null");
            return;
        }
        if (baseTable.tableIsExist()) {
            Util.d(TAG, baseTable.getTableName() + " has created!");
            return;
        }
        List<String> sqlList = new ArrayList<>();
        String sql = baseTable.onBuildSqlCmdForCreate();
        if (!TextUtils.isEmpty(sql)) {
            sqlList.add(sql);
            execSQL(sqlList, db);
            baseTable.onCreated(this);
        }
    }

    /**
     * 执行 SQL exec cmd
     */
    public void execSQL(List<String> sqlList, SQLiteDatabase db) {
        if (mCheckDatabaseAtAsserting) {
            Util.d(TAG, "execSQL() --- mCheckDatabaseAtAsserting is true");
            return;
        }
        if (sqlList != null && sqlList.size() > 0) {
            for (int i = 0; i < sqlList.size(); i++) {
                String sql = sqlList.get(i);
                if (!TextUtils.isEmpty(sql)) {
                    db.execSQL(sql);
                }
            }
        }
    }

    /**
     * 删除一个数据库表
     */
    public void dropTable(SQLiteDatabase db, String strTableName) {
        if (mCheckDatabaseAtAsserting) {
            Util.d(TAG, "dropTable() --- mCheckDatabaseAtAsserting is true");
            return;
        }
        try {
            db.execSQL("DROP TABLE IF EXISTS " + strTableName);
        } catch (SQLException ex) {
            Util.d(TAG, "can not  drop table " + strTableName);
            ex.printStackTrace();
        }
    }

    /**
     * 给某个数据库表增加一列
     */
    public void addColumn(BaseTable<?> baseTable, String column, String append)
            throws SQLException {// String tableName,
        if (baseTable == null || mCheckDatabaseAtAsserting) {
            Util.d(TAG,
                    "addColumn() --- baseTable == null || mCheckDatabaseAtAsserting is true");
            return;
        }
        baseTable.addColumn(column, append);
    }

    private boolean mCheckDatabaseAtAsserting = false;


    /**
     * 重写getWritableDatabase方法防止递归的出现
     */
    @Override
    public SQLiteDatabase getWritableDatabase() {
        final SQLiteDatabase db;
        if (mDefaultWritableDatabase != null) {
            db = mDefaultWritableDatabase;
        } else {
            db = super.getWritableDatabase();
        }
        return db;
    }

    /************
     * 工具类方法
     ***********/
    public static boolean isFile(File file) {
        return file.exists() && file.isFile();
    }

    public static boolean createNewDirectory(File file) {
        return !(file.exists() && file.isDirectory()) && file.mkdirs();
    }

    public static boolean deleteFile(String filePath) {
        if (filePath == null || filePath.length() < 1)
            return true;
        File file = new File(filePath);
        return deleteFile(file);
    }

    public static boolean deleteFile(File file) {
        if (!file.exists())
            return true;
        boolean flag = false;
        if (file.isFile())
            flag = file.delete();
        return flag;
    }
}
