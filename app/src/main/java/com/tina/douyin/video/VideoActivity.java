package com.tina.douyin.video;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.tina.douyin.R;
import com.tina.douyin.video.widget.VideoView;


public class VideoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        String path = getIntent().getStringExtra("path");
        if (TextUtils.isEmpty(path)){
            finish();
        }
        VideoView videoView = findViewById(R.id.videoView);
        videoView.setDataSource(path);
        videoView.startPlay();
    }
}
