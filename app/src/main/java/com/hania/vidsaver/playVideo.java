package com.hania.vidsaver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.VideoView;

public class playVideo extends AppCompatActivity {
    public VideoView videoPlay;
    public static String videopath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        videoPlay = findViewById(R.id.videoPlayer);


        playVideo();
    }

    public void  playVideo(){
        videoPlay.setVideoPath(videopath);
        videoPlay.start();
    }

}