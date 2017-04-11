package com.code.open.downloadmanager;

/**
 * ================================================
 * Created by zhaokai on 2017/4/10.
 * Email zhaokai1033@126.com
 * Describe :
 * ================================================
 */

public class ApkBean implements AbstractViewHolder.ItemType {

    public String icon;
    public String url;
    public String name;

    public ApkBean(String icon, String url, String name) {
        this.icon = icon;
        this.url = url;
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int itemType() {
        return 0;
    }
}
