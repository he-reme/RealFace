package com.example.h.RealFace;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveResultActivity extends AppCompatActivity {
    ImageView result; //결과 이미지
    ImageButton yesButton; //이미지 저장 버튼
    ImageButton noButton; //이미지 저장 취소 버튼

    private String filename; //전 액티비티에서 받아온 이미지 파일 이름

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_result);

        Intent intent = getIntent();
        filename = intent.getExtras().getString("filename");

        String file = Environment.getExternalStorageDirectory().getPath() + File.separator + filename;
        result = (ImageView) findViewById(R.id.result);
        File imgFile = new File(file);

        if (imgFile.exists()) {
            //결과 이미지 result뷰에 불러옴
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            result = (ImageView) findViewById(R.id.result);
            result.setImageBitmap(myBitmap);
        }

        yesButton = (ImageButton)findViewById(R.id.yesButton);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //예 버튼 클릭 시 이미지를 저장하고 GoHomeActivity로 이동
                save();
                Intent intent = new Intent(SaveResultActivity.this, GoHomeActivity.class);
                startActivity(intent);
            }
        });

        noButton = (ImageButton)findViewById(R.id.noButton);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //아니오 버튼 클릭 시 전 액티비티로 이동
                SaveResultActivity.super.onBackPressed();
            }
        });
    }

    private void save(){
        //다른 save 코드의 주석과 동일
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        final ImageView saveImage = (ImageView) findViewById(R.id.result);//캡쳐할영역(프레임레이아웃)

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
            Toast.makeText(getApplicationContext(), "폴더가 생성되었습니다.", Toast.LENGTH_SHORT).show();
        }

        SimpleDateFormat day = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        saveImage.buildDrawingCache();
        Bitmap saveImageview = saveImage.getDrawingCache();

        String s = day.format(date);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path + "/" + s + ".png");
            saveImageview.compress(Bitmap.CompressFormat.PNG, 100, fos);
            MediaStore.Images.Media.insertImage(getContentResolver(), saveImageview, s, "descripton");
            Toast.makeText(getApplicationContext(), "저장완료", Toast.LENGTH_SHORT).show();
            fos.flush();
            fos.close();
            saveImage.destroyDrawingCache();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
