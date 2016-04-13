package com.shao.camera.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;

import com.shao.camera.R;
import com.shao.camera.view.FullScreenVideoView;

import java.io.File;

public class VideoShowActivity extends AppCompatActivity {
    private FullScreenVideoView vvShow;
    private MediaController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_show);
        vvShow=(FullScreenVideoView) findViewById(R.id.vv_show);
        mController=new MediaController(this);
        File file=new File(getIntent().getStringExtra("url"));
        if(file.exists()){
            vvShow.setVideoPath(file.getAbsolutePath());
            vvShow.setMediaController(mController);
            mController.setMediaPlayer(vvShow);
        }
        vvShow.start();
    }
}
