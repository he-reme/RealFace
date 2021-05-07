package com.example.h.RealFace;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MediaPlayer player = MediaPlayer.create(this, R.raw.moremi);
        //moremi 음악파일을 배경음악으로 지정
        player.setVolume(0.8f, 0.8f); //배경음악 음량 조절
        player.setLooping(true); //반복 재생 설정
        player.start(); //음악 재생

        final ImageButton photoViewerActivityButton = (ImageButton)findViewById(R.id.makeButton);
        photoViewerActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //PhotoViewerActivity로 이동
                Intent intent = new Intent(MainActivity.this, PhotoViewerActivity.class);
                startActivity(intent);
            }
        });
    }
}



