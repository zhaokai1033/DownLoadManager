package com.code.open.download.core;

import java.util.List;

/**
 * ================================================
 * Created by zhaokai on 2017/4/7.
 * Email zhaokai1033@126.com
 * Describe :
 * ================================================
 */

@SuppressWarnings("unused")
public interface LoadCallBack {
    /**
     * 获取上次下载数据
     */
    List<DownLoadInfo> getLastLoadInfo();

    /**
     * 保存下载记录到本地
     */
    void saveDownLoadInfo(DownLoadInfo info);

    /**
     * 删除本地下载记录
     */
    void removeDownLoadInfo(DownLoadInfo info);

    /**
     * 自动安装时调用安装文件
     */
    void install(DownLoadInfo downloadInfo);
}
