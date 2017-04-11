package com.code.open.downloadmanager;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.code.open.download.DownLoadHelp;
import com.code.open.download.Util;
import com.code.open.download.core.DownLoadInfo;
import com.code.open.download.core.DownloadManager;
import com.code.open.download.core.DownloadRecordFace;

/**
 * ================================================
 * Created by zhaokai on 2017/4/10.
 * Email zhaokai1033@126.com
 * Describe :
 * ================================================
 */

public class DownLoadViewHolder extends AbstractViewHolder<ApkBean> {

    private static final String TAG = "Manager";

    private final RecyclerView.Adapter adapter;
    private final ImageView icon;
    private final Button bt;
    private final TextView title;
    private final TextView content;
    private final DownLoadHelp helper;

    public DownLoadViewHolder(ViewGroup parents, RecyclerView.Adapter adapter) {
        super(parents, R.layout.item_app);
        this.adapter = adapter;
        icon = ((ImageView) itemView.findViewById(R.id.icon));
        bt = ((Button) itemView.findViewById(R.id.bt));
        title = ((TextView) itemView.findViewById(R.id.title));
        content = ((TextView) itemView.findViewById(R.id.content));
        helper = DownLoadHelp.newInstance();
    }


    @Override
    public void setData(final ApkBean data) {
        helper.unRegisterDownloadObserver();
        Glide.with(icon.getContext())
                .load(data.icon)
                .placeholder(R.mipmap.ic_launcher)
                .error(new ColorDrawable(Color.DKGRAY))
                .into(icon);
        title.setText(data.name);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String t = ((Button) v).getText().toString();
                switch (t) {
                    case "继续":
                    case "下载":
                        Log.d("Manager:", "下载");
                        download(data);
                        break;
                    default:
                        Log.d("Manager:", "暂停");
                        helper.pause(data.url);
                        break;
                }
            }
        });
        helper.registerSingleObserver(data.url, new DownloadManager.DownloadObserver() {
            @Override
            public void onDownloadStateChange(DownLoadInfo downloadInfo) {
                Util.d(TAG, downloadInfo.getName() + " : " + DownLoadHelp.getStateString(downloadInfo.getState()));
                bt.setText(DownLoadHelp.getStateString(downloadInfo.getState()));
            }

            @Override
            public void onDownloadProgressChange(DownLoadInfo downloadInfo) {
                content.setText("Speed:" + downloadInfo.getSpeed() + " time:" + downloadInfo.getTimeDelay()
                        + "\nprogress:" + downloadInfo.getProgress() + " size:" + downloadInfo.getCurrentLength()
                        + "\n tag:" + downloadInfo.getTag());
            }

            @Override
            public void onDownloadTaskFinish() {

            }
        });
    }

    private void download(final ApkBean data) {
        helper.download(new DownloadRecordFace() {
            @Override
            public String getUrl() {
                return data.url;
            }

            @Override
            public String getName() {
                return data.name;
            }

            @Override
            public String getTag() {
                return data.icon;
            }
        });
    }
}
