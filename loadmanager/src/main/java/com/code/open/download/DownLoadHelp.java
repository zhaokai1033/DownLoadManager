package com.code.open.download;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.code.open.download.core.DownLoadInfo;
import com.code.open.download.core.DownloadManager;
import com.code.open.download.core.DownloadRecordFace;
import com.code.open.download.core.LoadCallBack;
import com.code.open.download.db.DbHelper;
import com.code.open.download.db.DownLoadTable;

import java.io.File;
import java.util.List;

import okhttp3.OkHttpClient;

/**
 * ================================================
 * Created by zhaokai on 2017/4/6.
 * Email zhaokai1033@126.com
 * Describe :
 * ================================================
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class DownLoadHelp implements LoadCallBack {

    private final DownloadManager downloadManager;
    private final static int pauseDown = 1000;

    private Context mContext;
    private DownloadManager.DownloadObserver mObserver;
    private String mUrl;
    public static final int STATE_NONE = 0;//未下载的状态
    public static final int STATE_WAITING = 1;//等待中的状态，就是任务已经创建并且添加，但是并木有执行run方法
    public static final int STATE_DOWNLOADING = 2;//下载中的状态
    public static final int STATE_PAUSE = 3;//暂停的状态
    public static final int STATE_FINISH = 4;//下载完成的状态
    public static final int STATE_ERROR = 5;//下载出错的状态
    public static final int STATE_INSTALL = 6;//安装完成

    private static final String[] STATE = new String[]{"下载", "等待", "暂停", "继续", "完成", "出错", "安装"};

    private final Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case pauseDown:
                    downloadManager.needWait(false);
                    break;
            }
            return false;
        }
    });
    private DbHelper dbHelper;

    private DownLoadHelp() {
        downloadManager = DownloadManager.getInstance();

    }

    public static DownLoadHelp newInstance() {
        return new DownLoadHelp();
    }

    public static String getStateString(int state) {

        if (state >= STATE.length || state < 0) {
            return "";
        }
        return STATE[state];

    }

    /**
     * 取消下载监听
     */
    public void unRegisterDownloadObserver() {
        if (!TextUtils.isEmpty(mUrl)) {
            downloadManager.unRegisterDownloadObserver(mUrl, mObserver);
        } else {
            downloadManager.unRegisterDownloadObserver(mObserver);
        }
    }


    /**
     * 开启下载任务
     */
    public void download(DownloadRecordFace appInfo) {
        downloadManager.download(appInfo);
    }

    /**
     * 暂停下载，也就是将下载任务从线程池中移除
     */
    public void pause(String url) {
        downloadManager.pause(url);
    }

    /**
     * 停止下载 删除下载数据
     *
     * @param url 下载唯一标识
     */
    public void stopDownload(String url) {
        downloadManager.stopDownload(url);
    }

    /**
     * 暂停所有的下载任务
     */
    public boolean pauseAll() {
        return downloadManager.pauseAll();
    }

    /**
     * 开启所有下载
     */
    public boolean startAll() {
        return downloadManager.startAll();
    }

    /**
     * 本地数据库管理使用
     */
    public DownLoadHelp withContext(Context context) {
        this.mContext = context.getApplicationContext();
        this.dbHelper = DbHelper.getInstance(context.getApplicationContext());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                downloadManager.localCache(DownLoadHelp.this);
            }
        }, 1000);
        return this;
    }

    /**
     * 是否开启自动安装
     */
    public DownLoadHelp withInstall(boolean flag) {
        downloadManager.autoInstall(flag);
        return this;
    }

    /**
     * 设置下载目录 默认 外储存根目录下的download 文件夹
     *
     * @param dir 缓存目录
     */
    public DownLoadHelp withDir(String dir, Context context) {
        downloadManager.withDir(dir);
        this.mContext = context.getApplicationContext();
        return this;
    }

    /**
     * 添加网络请求客户端
     */
    public DownLoadHelp withHttpClient(OkHttpClient client) {
        downloadManager.httpClient(client);
        return this;
    }

    /**
     * 设置本地下载记录的缓存
     */
    public DownLoadHelp withLoadCallBack(LoadCallBack loadCallBack) {
        downloadManager.localCache(loadCallBack);
        return this;
    }

//    /**
//     * 加载旧下载信息 仅第一次初始化需要加载 建议通过缓存自定加载
//     * 下载信息 以下载地址为key值不会重复添加
//     */
//    public DownLoadHelp loadOldTask(List<DownLoadInfo> list) {
//        downloadManager.loadOldTask(list);
//        return this;
//    }

    /**
     * 注册添加下载监听器对象
     */
    public void registerSingleObserver(DownloadManager.DownloadObserver observer) {
        this.mObserver = observer;
        downloadManager.registerDownloadObserver(observer);
    }

    /**
     * 注册单个下载地址的监听
     *
     * @param url      下载地址
     * @param observer 监听器
     */
    public void registerSingleObserver(String url, DownloadManager.DownloadObserver observer) {
        this.mUrl = url;
        this.mObserver = observer;
        downloadManager.registerDownLoadObserver(url, observer);
    }


    /**
     * 让下载暂停一段时间 使用场景，大量下载导致网络请求变慢
     *
     * @param needWait 是否需要暂停一段时间
     * @param mill     持续时间 只有当 {@param needWait}为true 时才会起效,主动开启下载
     */
    public void waitDownLoad(boolean needWait, long mill) {
        downloadManager.needWait(needWait);
        if (needWait) {
            handler.removeMessages(pauseDown);
            handler.sendEmptyMessageDelayed(pauseDown, mill);
        }

    }

    /**
     * 获取当前正在下载的任务
     *
     * @return 当前正在下载的人物
     */
    public List<DownLoadInfo> getDownloadInfo() {
        return downloadManager.getDownLoadInfo();
    }

    /**
     * 获取当前下载链接的下载数据
     *
     * @param url 下载链接
     */
    public DownLoadInfo getDownloadInfo(String url) {
        return downloadManager.getDownloadInfo(url);
    }

    /**
     * 下载数据 本地实现
     */
    @Override
    public List<DownLoadInfo> getLastLoadInfo() {
        return dbHelper.getTable(DownLoadTable.class).queryLoadTask();
    }

    @Override
    public void saveDownLoadInfo(DownLoadInfo info) {
        dbHelper.getTable(DownLoadTable.class).insert(info);
    }

    @Override
    public void removeDownLoadInfo(DownLoadInfo info) {
        dbHelper.getTable(DownLoadTable.class).deleteInfo(info);
    }

    @Override
    public void install(DownLoadInfo downloadInfo) {
        if (mContext != null) {
            Util.install(mContext, new File(downloadInfo.getPath()));
        }
    }
}
