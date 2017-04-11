package com.code.open.download.core;

import java.io.File;


/**
 * ================================================
 * Created by zhaokai on 16/7/26.
 * Email zhaokai1033@126.com
 * ================================================
 */
public class DownLoadInfo {

    private int state;                  //任务的下载状态
    private float progress;             //下载进度  (0~100.0)
    private long size;                  //总长度
    private long currentLength;         //已经下载的长度
//    private String id;                  //下载任务的唯一标识
    private String speed = "";          //下载速度
    private String timeDelay = "";      //剩余下载时间
    private String url;         //下载地址
    private String path;                //下载文件保存的绝对路径
    private String name;                //游戏名/文件名
    private String errorMsg;            //错误信息
    private String tag;                 //附加信息

    private String dateFirst;           //第一次下载时间
    private String netState;            //网络状态
    private String packageName;         //应用包名

    public static DownLoadInfo create(DownloadRecordFace info) {
        DownLoadInfo downloadInfo = new DownLoadInfo();
        downloadInfo.setUrl(info.getUrl());
        downloadInfo.setState(DownloadManager.STATE_NONE);//设置未下载的状态
        downloadInfo.setCurrentLength(0);
        downloadInfo.setPath(DownloadManager.DOWNLOAD_DIR + File.separator + info.getName() + ".apk.down");
        downloadInfo.setName(info.getName());
        downloadInfo.setTag(info.getTag());
        return downloadInfo;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String iconUrl) {
        this.tag = iconUrl;
    }

    public String getNetState() {
        return netState;
    }

    public void setNetState(String netState) {
        this.netState = netState;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getCurrentLength() {
        return currentLength;
    }

    public void setCurrentLength(long currentLength) {
        this.currentLength = currentLength;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String downloadUrl) {
        this.url = downloadUrl;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public float getProgress() {
        return progress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setTimeDelay(String timeDelay) {
        this.timeDelay = timeDelay;
    }

    public String getTimeDelay() {
        return timeDelay;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getDateFirst() {
        return dateFirst;
    }

    public void setDateFirst(String dateFirst) {
        this.dateFirst = dateFirst;
    }

    @Override
    public String toString() {
        return "DownLoadInfo{" +
                "state=" + state +
                ", progress=" + progress +
                ", size=" + size +
                ", currentLength=" + currentLength +
                ", speed='" + speed + '\'' +
                ", timeDelay='" + timeDelay + '\'' +
                ", url='" + url + '\'' +
                ", path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", tag='" + tag + '\'' +
                ", dateFirst='" + dateFirst + '\'' +
                ", netState='" + netState + '\'' +
                ", packageName='" + packageName + '\'' +
                '}';
    }
}
