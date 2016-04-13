package com.shao.camera.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.shao.camera.R;
import com.shao.camera.adapter.PhotoShowAdapter;
import com.shao.camera.utils.ListSheft;

import java.util.List;

public class PhotoShowActivity extends AppCompatActivity {
    private ViewPager vpPager;
    private PhotoShowAdapter mAdapter;
    private List<String> mList;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_show);
        mList = (List<String>) ListSheft.String2SceneList(getIntent().getStringExtra("list"));
        vpPager = (ViewPager) findViewById(R.id.vp_pager);
        vpPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
        mAdapter = new PhotoShowAdapter(this, mList, getIntent().getStringExtra("path"));
        vpPager.setAdapter(mAdapter);
        vpPager.setCurrentItem(getIntent().getIntExtra("positation", 0));
    }
}
