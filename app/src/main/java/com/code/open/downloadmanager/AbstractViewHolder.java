package com.code.open.downloadmanager;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * ================================================
 * Created by zhaokai on 2017/4/10.
 * Email zhaokai1033@126.com
 * Describe :
 * ================================================
 */

public abstract class AbstractViewHolder<T extends AbstractViewHolder.ItemType> extends RecyclerView.ViewHolder {

    public AbstractViewHolder(ViewGroup parent, @LayoutRes int res) {
        super(LayoutInflater.from(parent.getContext()).inflate(res, parent, false));
    }

    public abstract void setData(T data);

    public interface ItemType {
        int itemType();
    }
}
