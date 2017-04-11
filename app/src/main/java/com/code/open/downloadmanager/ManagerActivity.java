package com.code.open.downloadmanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.code.open.download.DownLoadHelp;
import com.code.open.download.Util;
import com.code.open.download.core.DownLoadInfo;

import java.util.ArrayList;
import java.util.List;

public class ManagerActivity extends AppCompatActivity {
    private static final String TAG = "ManagerActivity";

    private RecyclerView recycle;
    private List<ApkBean> list = new ArrayList<>();
    private MarketAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
        initRecycleView();
        initData();
    }

    private void initData() {
        List<DownLoadInfo> infos = DownLoadHelp.newInstance().getDownloadInfo();
        for (DownLoadInfo info : infos) {
            list.add(new ApkBean(info.getTag(), info.getUrl(), info.getName()));
        }
        adapter.notifyDataSetChanged();
    }

    private void initRecycleView() {
        recycle = ((RecyclerView) findViewById(R.id.recycle));
        recycle.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new MarketAdapter(list);
        recycle.setAdapter(adapter);
    }
}
