package com.code.open.download;

import android.content.Context;
import android.widget.Toast;

/**
 * ================================================
 * <p>
 * Created by zhaokai on 2017/3/3.
 * Email zhaokai1033@126.com
 * <p>
 * ================================================
 * <p>
 * 提示框工具类
 */

@SuppressWarnings("unused")
public class ToastUtil {

    private ToastUtil mInstance;
    private Toast mToast;
    private static ToastUtil t;

    private ToastUtil() {
    }

    private static ToastUtil getToastUtil(Context c) {
        if (t == null) {
            t = new ToastUtil();
        }
        return t;
    }

    public static void showToast(Context c, String msg) {
        if (c != null) {
            getToastUtil(c).showToastImp(c, msg);
        }
    }

    private void showToastImp(Context c, String text) {
        if (mToast == null) {
            mToast = Toast.makeText(c, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_LONG);
        }
        mToast.show();
    }

    public void cancelToastImp() {
        if (mToast != null) {
            mToast.cancel();
        }
    }
}
