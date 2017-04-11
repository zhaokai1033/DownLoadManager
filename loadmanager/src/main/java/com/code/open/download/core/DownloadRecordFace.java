package com.code.open.download.core;

/**
 * ================================================
 * <p/>
 * Created by zhaokai on 16/7/26.
 * Email zhaokai1033@126.com
 * <p/>
 * ================================================
 * 下载记录接口
 */
public interface DownloadRecordFace {

    //下载地址
    String getUrl();

    //要保存的文件名
    String getName();

    //备注信息
    String getTag();
}
