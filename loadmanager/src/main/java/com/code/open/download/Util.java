package com.code.open.download;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.util.Locale;

/**
 * ================================================
 * Created by zhaokai on 2017/4/7.
 * Email zhaokai1033@126.com
 * Describe :
 * ================================================
 */

public class Util {

    /**
     * 下载速度
     *
     * @param size 单位字节数
     * @return 2MB/s
     */
    public static String formatSize(long size) {

        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        long tb = gb * 1024;

        if (size >= tb) {
            return String.format(Locale.getDefault(), "%.1f TB/s", (float) size / tb);
        } else if (size >= gb) {
            return String.format(Locale.getDefault(), "%.1f GB/s", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB/s", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB/s", f);
        } else
            return String.format(Locale.getDefault(), "%d B/s", size);
    }

    /**
     * 格式化下载剩余时间
     */
    public static String loadTimeFormat(long second) {
        long hh = second / 3600;
        long mm = second % 3600 / 60;
        long ss = second % 60;
        String strTemp;
        if (0 != hh) {
            strTemp = String.format(Locale.getDefault(), "%02d小时%02d分%02d秒", hh, mm, ss);
        } else if (0 != mm) {
            strTemp = String.format(Locale.getDefault(), "%02d分%02d秒", mm, ss);
        } else {
            strTemp = String.format(Locale.getDefault(), "%02d秒", ss);
        }
        return strTemp;
    }

    /**
     * 安装一个apk文件
     */
    public static void install(Context context, File uriFile) {
        if (uriFile.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(uriFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (context != null)
                context.startActivity(intent);
        } else {
            ToastUtil.showToast(context, "安装包不存在");
        }
    }

    /**
     * 获取剩余可用空间
     */
    public static long getAvailableSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());

        long blockSize, availableBlocks;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return stat.getAvailableBytes();
        } else {
            int totalBlocks = stat.getBlockCount();
            blockSize = stat.getBlockSize();
            availableBlocks = stat.getAvailableBlocks();
        }
        return availableBlocks * blockSize;
    }

    /**
     * 打印日志
     */
    public static void d(String tag, String msg) {
        StringBuilder builder = new StringBuilder();

        StackTraceElement[] sElements = new Throwable().getStackTrace();
        builder.append("[")
                .append(sElements[1].getMethodName())
                .append(":")
                .append(sElements[1].getLineNumber())
                .append("]")
                .append(msg);

        Log.d(tag + tag, builder.toString());
    }
}
