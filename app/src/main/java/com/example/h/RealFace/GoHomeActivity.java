package com.example.h.RealFace;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

public class GoHomeActivity extends AppCompatActivity {

    ImageButton gohomeButton;//홈으로 가기 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_home);

        gohomeButton = (ImageButton)findViewById(R.id.gohomeButton);
        gohomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //MainActivity로 이동
                Intent intent = new Intent(GoHomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
