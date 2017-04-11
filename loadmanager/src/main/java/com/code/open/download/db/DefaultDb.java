package com.code.open.download.db;

import android.content.Context;

/**
 * ================================================
 * Created by zhaokai on 2017/4/11.
 * Email zhaokai1033@126.com
 * Describe :
 * ================================================
 * 下载默认数据库
 */

public class DefaultDb extends SqlDataBase {

    private static final String DB_NAME = "DefaultDb";
    private static final int VERSION = 1;

    public DefaultDb(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    protected void onRegisterTables() {
        //注册下载信息列表
        registerTable(DownLoadTable.class, new DownLoadTable(this));
    }
}
