package com.code.open.download.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.code.open.download.core.DownLoadInfo;
import com.code.open.download.core.DownloadManager;

import java.util.List;

/**
 * Created by zhaokai on 2016-05-12
 *
 * @Email zhaokai1033@126.com
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class DownLoadTable extends BaseTable<DownLoadInfo> {

    private static final String GAME_NAME = "gameName";
    private static final String PACKAGE_NAME = "packageName";
    private static final String SIZE = "size";
    private static final String PROGRESS = "progress";
    private static final String CURRENT_LENGTH = "currentLength";
    private static final String GAME_URL = "gameUrl";
    private static final String TAG = "tag";
    private static final String STATE_NET = "stateNet";
    private static final String STATE_LOAD = "stateLoad";
    private static final String GAME_PATH = "gamePath";
    private static final String STATE_SPEED = "speed";

    private static final String DATE_FIRST = "dateFirst";

    private static String TABLE_NAME = "DownLoadTable";

    public DownLoadTable(SqlDataBase sqlHelper) {
        super(TABLE_NAME, sqlHelper);
    }


    /**
     * 查询数据库并组成model 返回
     *
     * @param cursor 数据集
     * @return model
     */
    @Override
    public DownLoadInfo getItemFromCursor(Cursor cursor) {
        DownLoadInfo info = new DownLoadInfo();

        info.setSize(getValue(cursor, SIZE, Long.class));
        String progress = getValue(cursor, PROGRESS, String.class);
        float p = Float.parseFloat(progress);
        info.setProgress(p);
        info.setCurrentLength(getValue(cursor, CURRENT_LENGTH, Long.class));
        info.setPackageName(getValue(cursor, PACKAGE_NAME, String.class));
        info.setUrl(getValue(cursor, GAME_URL, String.class));
        info.setTag(getValue(cursor, TAG, String.class));
        info.setName(getValue(cursor, GAME_NAME, String.class));
        info.setPath(getValue(cursor, GAME_PATH, String.class));
        info.setSpeed("");
        info.setNetState(getValue(cursor, STATE_NET, String.class));
        info.setState(getValue(cursor, STATE_LOAD, Integer.class));
        info.setDateFirst(getValue(cursor, DATE_FIRST, String.class));
        return info;
    }

    /**
     * 创建 数据库
     */
    @Override
    public String onBuildSqlCmdForCreate() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SIZE + " INT8," +
                PROGRESS + " INT8," +
                CURRENT_LENGTH + " INT8," +
                PACKAGE_NAME + " TEXT," +
                GAME_URL + " TEXT," +
                GAME_NAME + " TEXT," +
                GAME_PATH + " TEXT," +
                STATE_SPEED + " TEXT," +
                STATE_NET + " TEXT," +
                DATE_FIRST + " TEXT," +
                STATE_LOAD + " INT8," +
                TAG + " TEXT," +

                "text_reserve1 TEXT," +
                "text_reserve2 TEXT," +
                "text_reserve3 TEXT," +
                "text_reserve4 TEXT," +
                "text_reserve5 TEXT," +
                "num_reserve1 INT8," +
                "num_reserve2 INT8," +
                "num_reserve3 INT8," +
                "num_reserve4 INT8," +
                "num_reserve5 INT8" +
                ")";
    }

    /**
     * 转换model 为数据库可用类型
     *
     * @param item model
     */
    @Override
    public ContentValues getContentValues(DownLoadInfo item) {
        ContentValues values = new ContentValues();
        values.put(SIZE, item.getSize());
        values.put(PROGRESS, item.getProgress() + "");
        values.put(CURRENT_LENGTH, item.getCurrentLength());
        values.put(PACKAGE_NAME, item.getPackageName());
        values.put(GAME_URL, item.getUrl());
        values.put(TAG, item.getTag());
        values.put(GAME_NAME, item.getName());
        values.put(GAME_PATH, item.getPath());
        values.put(STATE_LOAD, item.getState());
        values.put(STATE_NET, item.getNetState());
        values.put(STATE_SPEED, "");
        if (!TextUtils.isEmpty(item.getDateFirst()))
            values.put(DATE_FIRST, item.getDateFirst());
        return values;
    }

    @Override
    public void onCreated(SqlDataBase db) {
        super.onCreated(db);
    }

    /**
     * 增加条目
     *
     * @param column 列字段
     * @param append 类型
     */
    @Override
    public void addColumn(String column, String append) throws SQLException {
        super.addColumn(column, append);
    }

    /**
     * 数据库版本升级
     *
     * @param db           数据库
     * @param oldDBVersion 旧版本
     * @param newDBVersion 新版本
     */
    @Override
    public void onUpgraded(SqlDataBase db, int oldDBVersion, int newDBVersion) {
//        if(oldDBVersion<6){
//            addColumn(TAG,"TEXT");
//            addColumn(BEATID,"TEXT");
//        }

        super.onUpgraded(db, oldDBVersion, newDBVersion);
    }


    /**
     * 插入条目
     *
     * @param item 要插入的条目
     */
    @Override
    public long insert(DownLoadInfo item) {
        if (isExistByUrl(item.getUrl())) {                    //验证条目是否符合标准
            return updateItem(item);                //根据制定条件更新数据库
        } else {
            return super.insert(item);              //插入数据库
        }
    }

    /**
     * 更新条目         统一用 插入进行操作
     *
     * @param item 要更新的条目
     */
    private long updateItem(DownLoadInfo item) {
        if (TextUtils.isEmpty(item.getUrl())) {
            return -1;
        }
        String whereClause = GAME_URL + "=?";
        String[] whereArgs = new String[]{item.getUrl()};
        return updateByCase(item, whereClause, whereArgs);
    }

    /**
     * 判断数据库中是否有数据
     */
    public boolean isExistByUrl(String url) {
        List<DownLoadInfo> list = queryByGameUrl(url);
        return list != null && list.size() > 0;
    }

    /**
     * 通过指定条件删除条目
     */
    public void deleteByGameUrl(String url) {
        if (TextUtils.isEmpty(url))
            return;
        String whereClause = GAME_URL + "=?";
        String[] whereArgs = new String[]{url};
        deleteByCase(whereClause, whereArgs);
    }

    /**
     * 获取下载及下载后未安装的任务
     */
    public List<DownLoadInfo> queryTaskWithoutInstall() {
        String whereClause = STATE_LOAD + "<>?";
        String[] whereArgs = new String[]{DownloadManager.STATE_INSTALL + ""};
        return queryByCase(whereClause, whereArgs, DATE_FIRST + " asc");
    }

    /**
     * 获取下载中的任务
     */
    public List<DownLoadInfo> queryLoadTask() {
        String whereClause = STATE_LOAD + "<>? and " + STATE_LOAD + " <>?";
        String[] whereArgs = new String[]{DownloadManager.STATE_INSTALL + "", DownloadManager.STATE_FINISH + ""};
        return queryByCase(whereClause, whereArgs, null);
    }

    /**
     * 获取下载完成的任务
     */
    public List<DownLoadInfo> queryFinishTask() {
        String whereClause = STATE_LOAD + "=? or " + STATE_LOAD + " =?";
        String[] whereArgs = new String[]{DownloadManager.STATE_INSTALL + "", DownloadManager.STATE_FINISH + ""};
        return queryByCase(whereClause, whereArgs, DATE_FIRST + " desc");
    }

    /**
     * 通过制定参数查询条目
     *
     * @param url 制定字段的数据
     */
    public List<DownLoadInfo> queryByGameUrl(String url) {
        if (TextUtils.isEmpty(url))
            return null;
        String whereClause = GAME_URL + "=?";
        String[] whereArgs = new String[]{url};
        return queryByCase(whereClause, whereArgs, null);

    }

    // 用法示例
    public void demo() {
        DownLoadTable table = DbHelper.getInstance(null).getTable(DownLoadTable.class);
//        //查询所有数据
//        List<BeanUser> listAll = table.queryAll();
//        //查询gold == 123 的数据
//        List<BeanUser> listGold = table.queryByGold("123");
//        //插入或者更新 条目
//        BeanUser modelUser = new BeanUser();
//        table.insert(modelUser);

    }

    /**
     * 根据安装的报名更新下载数据库
     */
    public List<DownLoadInfo> queryByPackName(String packageName) {
        String whereClause = PACKAGE_NAME + "=?";
        String[] whereArgs = new String[]{packageName};
        return queryByCase(whereClause, whereArgs, null);
    }

    /**
     * 根据用户卸载行为,删除数据库里的下载信息
     */
    public int deleteInfo(DownLoadInfo info) {
        String whereClause = PACKAGE_NAME + "=?";
        String[] whereArgs = new String[]{info.getPackageName()};
        return deleteByCase(whereClause, whereArgs);
    }
}
