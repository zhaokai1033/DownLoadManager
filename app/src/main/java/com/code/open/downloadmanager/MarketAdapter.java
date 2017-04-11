package com.code.open.downloadmanager;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * Created by zhaokai on 2017/4/10.
 * Email zhaokai1033@126.com
 * Describe :
 * ================================================
 */

public class MarketAdapter extends RecyclerView.Adapter<DownLoadViewHolder> {


    private final List<ApkBean> mItems ;

    public MarketAdapter(List<ApkBean> list) {
        this.mItems = list;
    }


    @Override
    public DownLoadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DownLoadViewHolder(parent, this);
    }

    @Override
    public void onBindViewHolder(DownLoadViewHolder holder, int position) {
        holder.setData(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

}
