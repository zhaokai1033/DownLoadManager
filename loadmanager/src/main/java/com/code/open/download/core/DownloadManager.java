package com.code.open.download.core;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.code.open.download.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * 用来管理和维护下载任务的逻辑
 *
 * @author Administrator
 */
@SuppressWarnings({"WeakerAccess", "ResultOfMethodCallIgnored"})
public class DownloadManager {

    private static final String TAG = "DownloadManager";
    //定义文件的下载目录
    public static String DOWNLOAD_DIR = "";
    //所有观察者的默认key 值
    private final static String OBSERVER = "All_Observer";

    //定义6种下载状态常量
    public static final int STATE_NONE = 0;//未下载的状态
    public static final int STATE_WAITING = 1;//等待中的状态，就是任务已经创建并且添加，但是并木有执行run方法
    public static final int STATE_DOWNLOADING = 2;//下载中的状态
    public static final int STATE_PAUSE = 3;//暂停的状态
    public static final int STATE_FINISH = 4;//下载完成的状态
    public static final int STATE_ERROR = 5;//下载出错的状态
    public static final int STATE_INSTALL = 6;//安装完成

    //初始本类对象
    private static DownloadManager mInstance;
    private final Handler mHandler;

    //用于存放所有的下载监听器对象,注意：这里只是内存中存储数据，并木有进行持久化保存
    private HashMap<String, ArrayList<DownloadObserver>> observerMap = new HashMap<>();
    //用于存放每个任务的数据信息
    private HashMap<String, DownLoadInfo> downloadInfoMap = new HashMap<>();
    //用于存放每个DownloadTask对象，以便于暂停的时候可以找到对应的task，然后从线程池中移除，及时为缓冲队列的任务腾出系统资源
    private HashMap<String, DownloadTask> downloadTaskMap = new HashMap<>();
    //用于存放下载完成但尚未安装的任务
//    private List<DownLoadInfo> taskFinish = new ArrayList<>();
    private HashMap<String, DownLoadInfo> taskFinishMap = new HashMap<>();
    private boolean needWait;
    private LoadCallBack loadCallBackImp;
    private boolean isInstall;//是否自动安装

    private DownloadManager() {
        mHandler = new Handler(Looper.getMainLooper());
        DOWNLOAD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "DownLoadManager";
        File file = new File(DOWNLOAD_DIR);
        //创建下载目录
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();//创建多层目录
            Util.d("DownloadManager", "创建目录" + mkdirs);
        }
        observerMap.put(OBSERVER, new ArrayList<DownloadObserver>());
    }

    /**
     * 初始化缓存目录
     *
     * @param download_dir 缓存目录
     */
//    @RequiresPermission(allOf = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public DownloadManager withDir(String download_dir) {
        File file;
        if (!TextUtils.isEmpty(download_dir)) {
            file = new File(download_dir);
            DOWNLOAD_DIR = download_dir;
        } else {
            file = new File(DOWNLOAD_DIR);
        }
        //创建目录
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();//创建多层目录
            Util.d("DownloadManager", "创建目录" + mkdirs);
        }
        return this;
    }

    /**
     * 加载旧下载记录
     */
    public DownloadManager loadOldTask(List<DownLoadInfo> list) {
        if (list != null) {
            for (DownLoadInfo info : list) {
                int state = info.getState();
                //校验数据的有效性，防止下载过程中退出，第二次进入的时候，由于状态没有更新导致的状态错误
                if (STATE_PAUSE == state || STATE_WAITING == state || STATE_DOWNLOADING == state) {
                    info.setSpeed("");
                    info.setState(STATE_PAUSE);
                } else if (STATE_ERROR == state) {
                    info.setSpeed("");
                }
                if (STATE_FINISH == state) {
                    mInstance.addFinish(info);
                } else {
                    mInstance.insert(info);
                    notifyDownloadProgressChange(info);
                    notifyDownloadStateChange(info);
                }
            }
        }
        return this;
    }

    /**
     * 添加网络请求客户端
     */
    public DownloadManager httpClient(OkHttpClient client) {
        HttpHelper.setClient(client);
        return this;
    }

    public static DownloadManager getInstance() {
        if (mInstance == null) {
            synchronized (TAG) {
                if (mInstance == null) {
                    mInstance = new DownloadManager();
                    mInstance.loadLastData();
                }
            }
        }
        return mInstance;
    }

    private void loadLastData() {
        if (loadCallBackImp != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mInstance.loadOldTask(loadCallBackImp.getLastLoadInfo());
                }
            }).start();
        }
    }

    public List<DownLoadInfo> getDownLoadInfo() {

        List<DownLoadInfo> list = new ArrayList<>();

        for (String url : downloadInfoMap.keySet()) {
            list.add(downloadInfoMap.get(url));
        }
        for (String url : taskFinishMap.keySet()) {
            list.add(taskFinishMap.get(url));
        }
        return list;
    }

    /**
     * 获取下载信息
     */
    public DownLoadInfo getDownloadInfo(String url) {
        return downloadInfoMap.get(url);
    }

    /**
     * 安装完成更新下载完的游戏
     */
    public void updateFinish(DownLoadInfo info) {
        if (info != null && taskFinishMap != null) {
            taskFinishMap.remove(info.getUrl());
        }
    }

    /**
     * 下载完成后存储
     */
    private void addFinish(DownLoadInfo info) {
        if (taskFinishMap != null) {
            taskFinishMap.put(info.getUrl(), info);
        }
    }

    /**
     * 插入下载信息
     */
    private void insert(DownLoadInfo info) {
        if (!downloadInfoMap.containsValue(info)) {
            downloadInfoMap.put(info.getUrl(), info);
        }
    }

    /**
     * 开启下载任务
     */
    public void download(DownloadRecordFace appInfo) {
        //获取DownloadInfo对象，downloadInfo里面存放了下载的state和长度等等
        DownLoadInfo downloadInfo = downloadInfoMap.get(appInfo.getUrl());
        if (downloadInfo == null) {
            downloadInfo = DownLoadInfo.create(appInfo);
            downloadInfo.setDateFirst(System.currentTimeMillis() + "");
            downloadInfoMap.put(downloadInfo.getUrl(), downloadInfo);

            if (loadCallBackImp != null) {
                loadCallBackImp.saveDownLoadInfo(downloadInfo);
            }
        }
        //判断当前的state是否能够进行下载：none，pause,error
        if (downloadInfo.getState() == STATE_ERROR || downloadInfo.getState() == STATE_NONE || downloadInfo.getState() == STATE_PAUSE) {
            if (downloadInfo.getState() == STATE_ERROR) {
                File file = new File(downloadInfo.getPath());
                if (file.exists()) {
                    //删除错误文件
                    file.delete();
                }
            }
            DownloadTask downloadTask = new DownloadTask(downloadInfo);//新建下载任务；
            downloadInfo.setState(STATE_WAITING);//改状态信息
            notifyDownloadStateChange(downloadInfo);//发送状态更改广播
            ThreadPoolManager.getInstance().execute(downloadTask);//开启任务
            Util.d("DownloadManager", downloadInfo.getUrl() + " : " + downloadInfo.getPath());
        }
    }

    /**
     * 暂停下载，也就是将下载任务从线程池中移除
     */
    public void pause(String url) {
        DownLoadInfo downloadInfo = downloadInfoMap.get(url);
        if (downloadInfo != null) {
            downloadInfo.setState(STATE_PAUSE);//更改状态
            notifyDownloadStateChange(downloadInfo);//通知更新
            //移除任务
            ThreadPoolManager.getInstance().remove(downloadTaskMap.get(downloadInfo.getUrl()));
        }
    }

    /**
     * 停止下载 删除下载数据
     *
     * @param url 下载唯一标识
     */
    public void stopDownload(final String url) {
        DownLoadInfo downloadInfo = downloadInfoMap.get(url);
        if (downloadInfo != null) {
            downloadInfoMap.remove(url);
            downloadInfo.setState(STATE_PAUSE);//更改状态
            notifyDownloadStateChange(downloadInfo);//通知更新
            //移除任务
            ThreadPoolManager.getInstance().remove(downloadTaskMap.get(downloadInfo.getUrl()));
        }
        downloadTaskMap.remove(url);

        if (loadCallBackImp != null) {
            loadCallBackImp.removeDownLoadInfo(downloadInfo);
        }


        if (downloadInfo == null) {
            downloadInfo = taskFinishMap.get(url);
        }

        if (downloadInfo != null) {
            //删除下载文件
            final String path = downloadInfo.getPath();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!TextUtils.isEmpty(path)) {
                            File file = new File(path);
                            file.delete();
                        }
                    } catch (Exception e) {
                        Util.d("DownloadManager", e.toString());
                    }
                }
            }).start();
        }
    }

    /**
     * 暂停所有的下载任务
     */
    public boolean pauseAll() {
        if (downloadInfoMap.size() > 0) {
            for (String url : downloadInfoMap.keySet()) {
                DownLoadInfo downloadInfo = downloadInfoMap.get(url);
                if (downloadInfo != null) {
                    downloadInfo.setState(STATE_PAUSE);//更改状态
                    notifyDownloadStateChange(downloadInfo);//通知更新
                    //移除任务
                    ThreadPoolManager.getInstance().remove(downloadTaskMap.get(downloadInfo.getUrl()));
                }
            }
            return true;
        }
        return false;
    }

    public boolean startAll() {
        if (downloadInfoMap.size() > 0 && downloadTaskMap.size() <= 0) {
            for (String url : downloadInfoMap.keySet()) {
                DownLoadInfo downloadInfo = downloadInfoMap.get(url);
                //判断当前的state是否能够进行下载：none，pause,error
                if (downloadInfo.getState() == STATE_ERROR || downloadInfo.getState() == STATE_NONE || downloadInfo.getState() == STATE_PAUSE) {
                    if (downloadInfo.getState() == STATE_ERROR) {
                        File file = new File(downloadInfo.getPath());
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                    DownloadTask downloadTask = new DownloadTask(downloadInfo);//新建下载任务；
                    downloadInfo.setState(STATE_WAITING);//改状态信息
                    notifyDownloadStateChange(downloadInfo);//发送状态更改广播
                    ThreadPoolManager.getInstance().execute(downloadTask);//开启任务
                    Util.d(TAG, downloadInfo.getUrl() + " : " + downloadInfo.getPath());
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 取消注册下载监听器对象
     */
    public void unRegisterDownloadObserver(DownloadObserver observer) {
        ArrayList<DownloadObserver> observerList = observerMap.get(OBSERVER);
        if (observerList != null && observerList.contains(observer))
            observerList.remove(observer);
    }

    /**
     * 注册添加下载监听器对象
     */
    public void registerDownloadObserver(DownloadObserver observer) {
        ArrayList<DownloadObserver> observerList = observerMap.get(OBSERVER);
        if (observerList == null) {
            observerList = new ArrayList<>();
            observerMap.put(OBSERVER, observerList);
        }
        if (!observerList.contains(observer)) {
            observerList.add(observer);
        }
    }

    /**
     * 注册单个下载地址的监听
     *
     * @param url      下载地址
     * @param observer 监听器
     */
    public void registerDownLoadObserver(String url, DownloadObserver observer) {
        if (!TextUtils.isEmpty(url)) {
            ArrayList<DownloadObserver> observerList = observerMap.get(url);
            if (observerList == null) {
                observerList = new ArrayList<>();
                observerMap.put(url, observerList);
            }
            observerList.add(observer);
            DownLoadInfo info = downloadInfoMap.get(url);
            if (info != null) {
                notifyDownloadProgressChange(info);
                notifyDownloadStateChange(info);
            }
        }
    }

    /**
     * 取消单个下载地址的监听
     *
     * @param url      下载地址
     * @param observer 监听器
     */
    public void unRegisterDownloadObserver(String url, DownloadObserver observer) {
        if (!TextUtils.isEmpty(url)) {
            ArrayList<DownloadObserver> observerList = observerMap.get(url);
            if (observerList != null) {
                if (observerList.contains(observer)) {
                    observerList.remove(observer);
                }
                if (observerList.size() == 0) {
                    observerMap.remove(url);
                }
            }
        }
    }

    /**
     * 本地缓存实现类
     * 此方法会开起子线程加载 初始数据
     */
    public void localCache(LoadCallBack loadCallBackImp) {
        this.loadCallBackImp = loadCallBackImp;
        loadLastData();
    }

    public void autoInstall(boolean flag) {
        this.isInstall = flag;
    }

    /**
     * 下载的监听器
     *
     * @author Administrator
     */
    public interface DownloadObserver {
        /**
         * 当前下载状态改变的回调
         */
        void onDownloadStateChange(DownLoadInfo downloadInfo);

        /**
         * 当下载进度更新的回调
         */
        void onDownloadProgressChange(DownLoadInfo downloadInfo);

        /**
         * 所有的下载任务结束
         */
        void onDownloadTaskFinish();
    }


    /**
     * 下载任务的bean对象
     *
     * @author Administrator
     */
    class DownloadTask implements Runnable {

        private DownLoadInfo downloadInfo;
        private long lastRefreshUiTime;         //上次更新时间
        private long lastDownloadLength = 0;    //本次已下载的大小
        private long currentDownLoadStartTime;  //当前下载耗时
        private long currentDownLoadLength;     //当前下载大小
        private long currentRefreshTime = 0;    //上次刷新时间

        public DownloadTask(DownLoadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void run() {
            //3.将state更改为下载中的状态
            downloadInfo.setState(STATE_DOWNLOADING);
            downloadInfo.setErrorMsg("");
            //通知监听器下载状态改变
            notifyDownloadStateChange(downloadInfo);

            //4.开始下载操作,2种情况：a.从头下载       b.断点下载
            HttpHelper.HttpResult httpResult;
            File file = new File(downloadInfo.getPath());
            Util.d(TAG, "FileSize:" + file.length() + "currentLength " + downloadInfo.getCurrentLength());
            if (!file.exists() || file.length() != downloadInfo.getCurrentLength()) {
                //从头下载的情况
                Util.d(TAG, "从头下载");
                file.delete();//删除错误文件
                downloadInfo.setCurrentLength(0);//重置已经下载的长度
                httpResult = HttpHelper.download(downloadInfo.getUrl());
            } else {
                Util.d(TAG, "断点下载");
                httpResult = HttpHelper.download(downloadInfo.getUrl(), file.length() + "");
            }

            //5.获取流对象，进行文件读写
            if (httpResult != null && httpResult.getInputStream() != null) {
                //说明获取文件数据成功，可以进行文件读写
                InputStream is = httpResult.getInputStream();
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file, true);//使用追加的方式

                    Util.d(TAG, "剩余空间" + Util.formatSize(Util.getAvailableSize()) + "FileSize: " + Util.formatSize(file.length()) + "  TotalSize " + Util.formatSize(httpResult.getTotalSize()));
                    if (httpResult.getTotalSize() > 0) {
                        downloadInfo.setSize(httpResult.getTotalSize() + file.length());
                    } else {
                        downloadInfo.setSize(1);
                    }
                    if (Util.getAvailableSize() < httpResult.getTotalSize()) {
                        file.delete();//删除文件
                        downloadInfo.setErrorMsg("剩余空间不足(还需：" + Util.formatSize(httpResult.getTotalSize()) + "剩余：" + Util.formatSize(Util.getAvailableSize()) + ")");
                        downloadInfo.setState(STATE_ERROR);//将state更改为失败
                        downloadInfo.setCurrentLength(0);//重置已经下载的长度
                        downloadTaskMap.remove(downloadInfo.getUrl());
                        notifyDownloadStateChange(downloadInfo);//通知监听器回调
                        return;
                    }
                    Util.d(TAG, "FileSize: " + Util.formatSize(file.length()) + "  TotalSize " + Util.formatSize(httpResult.getTotalSize()));

                    byte[] buffer = new byte[512];//0.5k的缓冲区
                    int len;
                    currentRefreshTime = System.currentTimeMillis();
                    currentDownLoadStartTime = System.currentTimeMillis();
                    while ((len = is.read(buffer)) != -1 && downloadInfo.getState() == STATE_DOWNLOADING) {
                        fos.write(buffer, 0, len);
                        //更新currentLength
                        currentDownLoadLength = currentDownLoadLength + len;
                        downloadInfo.setCurrentLength(downloadInfo.getCurrentLength() + len);
                        if (System.currentTimeMillis() - lastRefreshUiTime > 200) {
                            if (System.currentTimeMillis() - currentRefreshTime > 1000) {
                                long speedLong = currentDownLoadLength * 1000 / (System.currentTimeMillis() - currentDownLoadStartTime); // B/s
                                String speed = Util.formatSize(speedLong);
                                //更新下载速度
                                if (!TextUtils.isEmpty(speed)) {
                                    downloadInfo.setSpeed(speed);
                                }

                                long timeDelayMil;
                                String timeDelay;
                                if (speedLong > 0 && currentDownLoadLength - lastDownloadLength > 0) {
                                    timeDelayMil = (downloadInfo.getSize() - downloadInfo.getCurrentLength()) / speedLong;
                                    timeDelay = Util.loadTimeFormat(timeDelayMil);
                                } else {
                                    timeDelay = "连接中……";
                                    downloadInfo.setSpeed("");
                                }
                                currentRefreshTime = System.currentTimeMillis();
                                lastDownloadLength = currentDownLoadLength;
                                downloadInfo.setTimeDelay(timeDelay);
                            }
                            //需要通知监听器下载进度更新
                            notifyDownloadProgressChange(downloadInfo);
                            lastRefreshUiTime = System.currentTimeMillis();
                        }
                        while (isNeedWait()) {
                            Util.d(TAG, "等待中:" + downloadInfo.getName());
                            Thread.sleep(50);
                        }
                    }
                } catch (FileNotFoundException fileException) {
                    downloadInfo.setState(STATE_ERROR);
                    downloadInfo.setErrorMsg("读写失败，请开启读写权限");
                } catch (SocketTimeoutException socketTimeout) {
                    downloadInfo.setState(STATE_PAUSE);
                    downloadInfo.setErrorMsg("下载连接超时");
                    Util.d(TAG, "下载中连接超时:");
                } catch (SocketException socketException) {
                    downloadInfo.setState(STATE_PAUSE);
                    downloadInfo.setErrorMsg("网络连接断开");
                    Util.d(TAG, "下载中网络断开:");
                } catch (Exception e) {
                    e.printStackTrace();
                    //如果出异常，按照下载出错的情况处理
                    file.delete();//删除文件
                    downloadInfo.setCurrentLength(0);//重置已经下载的长度
                    downloadInfo.setState(STATE_ERROR);//将state更改为失败
                } finally {
                    notifyDownloadStateChange(downloadInfo);//通知监听器回调
                    //关闭流和链接的方法
                    httpResult.close();
                    try {
                        if (fos != null) fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //6.while循环结束了，会走到这里,3种情况：a.下载完成   b.暂停   c.出错
                if (downloadInfo.getState() == STATE_DOWNLOADING) {//file.length()==downloadInfo.getSize() &&
                    //下载完成的情况
                    downloadInfo.setState(STATE_FINISH);//将state更改为完成
                    int pce = ((int) (downloadInfo.getCurrentLength() * 100 / downloadInfo.getSize()));
                    downloadInfo.setProgress(pce);
                    downloadInfo.setPath(reNameFile(downloadInfo));
                    notifyDownloadStateChange(downloadInfo);//通知监听器回调
                    if (isInstall) {
                        if (loadCallBackImp != null) {
                            loadCallBackImp.install(downloadInfo);
                        }
                    }
                } else if (downloadInfo.getState() == STATE_PAUSE) {
                    notifyDownloadStateChange(downloadInfo);//主动暂停
                } else if (file.length() != downloadInfo.getCurrentLength()) {//删除错误文件
                    Util.d(TAG, "下载文件长度不一致");
                    //说明下载失败
                    file.delete();//删除文件
                    downloadInfo.setCurrentLength(0);//重置已经下载的长度
                    downloadInfo.setState(STATE_ERROR);//将state更改为失败
                    notifyDownloadStateChange(downloadInfo);//通知监听器回调
                }
            } else if (httpResult == null) {
                //说明下载失败
                downloadInfo.setErrorMsg("下载地址出错");
                file.delete();//删除文件
                downloadInfo.setState(STATE_ERROR);//将state更改为失败
                downloadInfo.setCurrentLength(0);//重置已经下载的长度
                notifyDownloadStateChange(downloadInfo);//通知监听器回调
            } else {
                downloadInfo.setErrorMsg("下载出错");
                file.delete();//删除文件
                downloadInfo.setState(STATE_ERROR);//将state更改为失败
                downloadInfo.setCurrentLength(0);//重置已经下载的长度
                notifyDownloadStateChange(downloadInfo);//通知监听器回调
            }

            //当run方法结束后，移除下载任务;需要将downloadTask从downloadTaskMap中移除
            downloadTaskMap.remove(downloadInfo.getUrl());
            //如果是下载完成 则移除下载信息
            if (DownloadManager.STATE_FINISH == downloadInfo.getState()) {
                downloadInfoMap.remove(downloadInfo.getUrl());
                Util.d(TAG, downloadInfo.getName() + "游戏下载完成" + (downloadInfoMap.get(downloadInfo.getUrl()) == null));
            } else {
                Util.d(TAG, downloadInfo.getName() + "游戏未完成" + (downloadInfo.getState()));
            }
            if (downloadTaskMap.size() <= 0) {
                notifyDownloadTaskFinish();
            }
        }


    }

    /**
     * 重命名下载的文件
     */
    private String reNameFile(DownLoadInfo downLoadInfo) {
        String newName = DownloadManager.DOWNLOAD_DIR
                + File.separator
                + downLoadInfo.getName()
                + ".apk";
        if (new File(downLoadInfo.getPath()).renameTo(new File(newName))) {
            return newName;
        } else {
            return downLoadInfo.getPath();
        }
    }

    /**
     * 是否需要暂停下载
     */
    private boolean isNeedWait() {
        return needWait;
    }

    public void needWait(boolean needWait) {
        this.needWait = needWait;
    }

    /**
     * 主线程中通知监听器更新状态
     */
    public void notifyDownloadStateChange(final DownLoadInfo downloadInfo) {
        if (loadCallBackImp != null) {
            loadCallBackImp.saveDownLoadInfo(downloadInfo);
        }
        if (downloadInfoMap.containsKey(downloadInfo.getUrl()))
            if (downloadInfo.getState() == DownloadManager.STATE_FINISH) {
                taskFinishMap.put(downloadInfo.getUrl(), downloadInfo);
            }
        if (downloadInfoMap.containsKey(downloadInfo.getUrl())) {
            //通知单个
            if (observerMap.containsKey(downloadInfo.getUrl())) {
                ArrayList<DownloadObserver> observerList = observerMap.get(downloadInfo.getUrl());
                for (final DownloadObserver observer : observerList) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            observer.onDownloadStateChange(downloadInfo);
                        }
                    });
                }
            }
            //通知所有的
            ArrayList<DownloadObserver> observerList = observerMap.get(OBSERVER);
            if (observerList != null)
                for (final DownloadObserver observer : observerList) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            observer.onDownloadStateChange(downloadInfo);
                        }
                    });
                }
        }
    }

    /**
     * 通知所有的监听器下载进度改变了
     */
    public void notifyDownloadProgressChange(final DownLoadInfo downloadInfo) {
        float pce = ((float) (downloadInfo.getCurrentLength() * 100) / (float) downloadInfo.getSize());
        try {
            BigDecimal b = new BigDecimal(pce);
            float p = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
            downloadInfo.setProgress(p);
        } catch (Exception e) {
            downloadInfo.setProgress(0);
        }
        if (loadCallBackImp != null) {
            loadCallBackImp.saveDownLoadInfo(downloadInfo);
        }
        if (observerMap.containsKey(downloadInfo.getUrl())) {
            ArrayList<DownloadObserver> observerList = observerMap.get(downloadInfo.getUrl());
            for (final DownloadObserver observer : observerList) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        observer.onDownloadProgressChange(downloadInfo);
                    }
                });
            }
        }
        //通知所有的
        ArrayList<DownloadObserver> observerList = observerMap.get(OBSERVER);
        for (final DownloadObserver observer : observerList) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    observer.onDownloadProgressChange(downloadInfo);
                }
            });
        }
    }

    /**
     * 通知所有的监听器下载任务结束了
     */
    public void notifyDownloadTaskFinish() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Set<String> observerSet = observerMap.keySet();
                    for (String url : observerSet) {
                        if (observerMap.containsKey(url) && observerMap.get(url).size() > 0)
                            for (final DownloadObserver observer : observerMap.get(url)) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        observer.onDownloadTaskFinish();
                                    }
                                });
                            }
                    }
                } catch (Exception e) {
                    Util.d("DownloadManager", e.toString());
                }
            }
        });
    }

    public static class HttpHelper {
        private static HttpHelper helper = new HttpHelper();
        public OkHttpClient httpClient;

        private HttpHelper() {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(3, TimeUnit.MINUTES);
            builder.readTimeout(3, TimeUnit.MINUTES);
//            builder.writeTimeout(3, TimeUnit.MINUTES);
            httpClient = builder.build();
        }

        public static void setClient(OkHttpClient client) {
            HttpHelper.helper.httpClient = client;
        }

        /**
         * 断点下载文件，返回流对象
         */
        public static HttpResult download(String url, String position) {
            return helper.downloadImp(url, position);
        }

        private HttpResult downloadImp(String url, String position) {

            Request request = new Request.Builder()
                    .url(url)
                    .header("Connection", "keep-alive")
                    .header("Range", "bytes=" + position + "-")
                    .build();
            try {
                Response httpResponse = httpClient
                        .newCall(request)
                        .execute();
                if (httpResponse != null) {
                    return new HttpResult(httpResponse);
                }

            } catch (Exception e) {
                return null;
            }
            return null;
        }

        /**
         * 下载文件，返回流对象
         */
        public static HttpResult download(String url) {
            return helper.downloadImp(url);
        }

        private HttpResult downloadImp(String url) {
            try {
                Request request = new Request.Builder()
                        .header("Connection", "keep-alive")
                        .url(url)
                        .build();
                Response httpResponse = httpClient
                        .newCall(request)
                        .execute();
                if (httpResponse != null) {
                    return new HttpResult(httpResponse);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Util.d("DownloadManager", "download: " + e.getMessage());
            }
//		}
            return null;
        }

        /**
         * Http返回结果的进一步封装
         *
         * @author Administrator
         */
        public static class HttpResult {
            private Response httpResponse;
            private InputStream inputStream;


            public HttpResult(Response httpResponse) {
                this.httpResponse = httpResponse;
            }

            public long getTotalSize() {
                if (httpResponse == null || httpResponse.body() == null) {
                    return 0;
                }
                return httpResponse.body().contentLength();
            }

            /**
             * 获取状态码
             */
            public int getStatusCode() {
                return httpResponse.code();
            }

            /**
             * 获取输入流
             */
            public InputStream getInputStream() {
                if (inputStream == null && getStatusCode() < 300) {
                    ResponseBody entity = httpResponse.body();
                    try {
                        inputStream = entity.byteStream();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Util.d("DownloadManager", "getInputStream: " + e.getMessage());
                    }
                }
                return inputStream;
            }

            /**
             * 关闭链接和流对象
             */
            public void close() {

                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Util.d("DownloadManager", "close: " + e.getMessage());
                    }
                }

            }
        }
    }
}
