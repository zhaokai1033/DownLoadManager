package com.code.open.downloadmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.code.open.download.DownLoadHelp;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recycle;
    private List<ApkBean> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownLoadHelp.newInstance().withContext(getApplicationContext()).withInstall(true);

        findViewById(R.id.bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ManagerActivity.class));
            }
        });
        initRecycleView();
    }

    private void initRecycleView() {
        list.add(new ApkBean("http://static.yingyonghui.com/icon/128/4200197.png"
                , "http://dldir1.qq.com/weixin/android/weixin6330android920.apk"
                , "微信"));
        list.add(new ApkBean("http://image.ylyq.duoku.com/uploads/images/2016/0601/1464717094669547.png"
                , "http://down.s.qq.com/download/1104466820/apk/10024873_com.tencent.tmgp.sgame.apk"
                , "王者荣耀"));
        list.add(new ApkBean("http://image.ylyq.duoku.com/uploads/images/2016/0906/1473132711356595.png"
                , "http://signd.bce.baidu-mgame.com/game/cloud/1501000/1501538/20160727181724_DuoKu.apk"
                , "阴阳师"));
        list.add(new ApkBean("http://image.ylyq.duoku.com/uploads/images/2016/0913/1473734964109574.png"
                , "http://manuallyclientyd.dl.wanmei.com/client/qyz1109_signed_laohu.apk"
                , "青云志"));
        list.add(new ApkBean("http://image.ylyq.duoku.com/uploads/images/2017/0223/1487843349270969.png",
                "http://duokoo.baidu.com/game/?pageid=Hdkicssp&p_tag=1716351",
                "权力与荣耀"));
        recycle = ((RecyclerView) findViewById(R.id.recycle));
        recycle.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recycle.setAdapter(new MarketAdapter(list));
    }
}
