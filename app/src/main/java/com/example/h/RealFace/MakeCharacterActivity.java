package com.example.h.RealFace;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MakeCharacterActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton saveButton; //저장 버튼

    ImageView face; //얼굴
    ImageView hair; //머리
    ImageView shirts; //상의
    ImageView pants; //하의
    ImageView shoes; //신발

    //머리종류 3개 버튼
    ImageButton hair1Button;
    ImageButton hair2Button;
    ImageButton hair3Button;

    //상의종류 3개 버튼
    ImageButton shirts1Button;
    ImageButton shirts2Button;
    ImageButton shirts3Button;

    //하의종류 3개 버튼
    ImageButton pants1Button;
    ImageButton pants2Button;
    ImageButton pants3Button;

    //신발종류 3개 버튼
    ImageButton shoes1Button;
    ImageButton shoes2Button;
    ImageButton shoes3Button;

    private String filename; //전 액티비티에서 받아온 파일 이름

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_character);

        Intent intent = getIntent();
        filename = intent.getExtras().getString("filename"); //전 액티비티에서 파일 이름 불러옴

        String file = Environment.getExternalStorageDirectory().getPath() + File.separator + filename;
        face = (ImageView) findViewById(R.id.face); //
        File imgFile = new File(file);

        if (imgFile.exists()) {
            //불러온 이미지 파일 있으면
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8; //사이즈 조절하지 않을 시 memoryout
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            face = (ImageView) findViewById(R.id.face);
            face.setImageBitmap(myBitmap); //이미지 얼굴뷰에 불러옴
        }

        MediaPlayer player = MediaPlayer.create(this, R.raw.character);
        //moremi 음악파일을 배경음악으로 지정
        player.setVolume(0.8f, 0.8f); //배경음악 음량 조절
        player.setLooping(true); //반복 재생 설정
        player.start(); //음악 재생

        init(); //초기화

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost); //탭 뷰 구현을 위한 TabHost
        tabHost.setup();

        TabHost.TabSpec hairTab = tabHost.newTabSpec("tab1"); //머리 탭 구현
        hairTab.setContent(R.id.tab1); //tab 내용
        hairTab.setIndicator("머리"); //tab 이름 설정
        tabHost.addTab(hairTab); //tab 등록

        TabHost.TabSpec shirtsTab = tabHost.newTabSpec("tab2"); //상의 탭 구현
        shirtsTab.setContent(R.id.tab2);
        shirtsTab.setIndicator("상의");
        tabHost.addTab(shirtsTab);

        TabHost.TabSpec pantsTab = tabHost.newTabSpec("tab3"); //하의 탭 구현
        pantsTab.setContent(R.id.tab3);
        pantsTab.setIndicator("하의");
        tabHost.addTab(pantsTab);

        TabHost.TabSpec shoesTab = tabHost.newTabSpec("tab4"); //신발 탭 구현
        shoesTab.setContent(R.id.tab4);
        shoesTab.setIndicator("신발");
        tabHost.addTab(shoesTab);

        tabHost.setCurrentTab(0); //처음 생성 된 탭이 처음 보여짐

        saveButton = (ImageButton) findViewById(R.id.saveButton);  //SaveResultActivity로 이동하는 버튼
        saveButton.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View view){
                save();
                Intent intent = new Intent(MakeCharacterActivity.this, SaveResultActivity.class);

                intent.putExtra("filename",filename);
                startActivity(intent);
            }
  //          @Override
   //         public void onClick(View view) {
  //              Log.e(this.getClass().getName(),"눌리기는 되는가..");
   //             save();
   //         }
        });
    }


    void init(){
        //머리, 상의, 하의, 신발 객체들 초기화 및 setOnClickListener

        hair = (ImageView)findViewById(R.id.hair);
        shirts = (ImageView)findViewById(R.id.shirts);
        pants = (ImageView)findViewById(R.id.pants);
        shoes = (ImageView)findViewById(R.id.shoes);

        hair1Button=(ImageButton)findViewById(R.id.hair1Button);
        hair1Button.setOnClickListener(this);

        hair2Button=(ImageButton)findViewById(R.id.hair2Button);
        hair2Button.setOnClickListener(this);

        hair3Button=(ImageButton)findViewById(R.id.hair3Button);
        hair3Button.setOnClickListener(this);

        shirts1Button=(ImageButton)findViewById(R.id.shirts1Button);
        shirts1Button.setOnClickListener(this);

        shirts2Button=(ImageButton)findViewById(R.id.shirts2Button);
        shirts2Button.setOnClickListener(this);

        shirts3Button=(ImageButton)findViewById(R.id.shirts3Button);
        shirts3Button.setOnClickListener(this);

        pants1Button=(ImageButton)findViewById(R.id.pants1Button);
        pants1Button.setOnClickListener(this);

        pants2Button=(ImageButton)findViewById(R.id.pants2Button);
        pants2Button.setOnClickListener(this);

        pants3Button=(ImageButton)findViewById(R.id.pants3Button);
        pants3Button.setOnClickListener(this);

        shoes1Button=(ImageButton)findViewById(R.id.shoes1Button);
        shoes1Button.setOnClickListener(this);

        shoes2Button=(ImageButton)findViewById(R.id.shoes2Button);
        shoes2Button.setOnClickListener(this);

        shoes3Button=(ImageButton)findViewById(R.id.shoes3Button);
        shoes3Button.setOnClickListener(this);
    }
    public void onClick(View v) {
        //해당 버튼 클릭 시 해당하는 버튼의 이미지 캐릭터에 입혀짐
        //지정했던 id를 확인하여 이미지 리소스 변경하는 방법

        switch (v.getId()){
            case R.id.hair1Button:
               hair.setImageResource(R.drawable.hair1);

                break;

            case R.id.hair2Button:
                hair.setImageResource(R.drawable.hair2);

                break;
            case R.id.hair3Button:
                hair.setImageResource(R.drawable.hair3);

                break;
            case R.id.shirts1Button:
                shirts.setImageResource(R.drawable.shirts1);

                break;
            case R.id.shirts2Button:
                shirts.setImageResource(R.drawable.shirts2);

                break;
            case R.id.shirts3Button:
                shirts.setImageResource(R.drawable.shirts3);

                break;
            case R.id.pants1Button:
                pants.setImageResource(R.drawable.pants1);

                break;
            case R.id.pants2Button:
                pants.setImageResource(R.drawable.pants2);

                break;
            case R.id.pants3Button:
                pants.setImageResource(R.drawable.pants3);

                break;
            case R.id.shoes1Button:
                shoes.setImageResource(R.drawable.shoes1);

                break;
            case R.id.shoes2Button:
                shoes.setImageResource(R.drawable.shoes2);

                break;
            case R.id.shoes3Button:
                shoes.setImageResource(R.drawable.shoes3);

                break;
        }
    }

    private void save(){
        //다른 save 코드의 주석과 동일
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        final FrameLayout saveImage = (FrameLayout) findViewById(R.id.saveView);//캡쳐할영역(프레임레이아웃)

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
        filename = s + ".png";
        try {
            fos = new FileOutputStream(path + "/" + s + ".png");
            saveImageview.compress(Bitmap.CompressFormat.PNG, 100, fos);
  //          MediaStore.Images.Media.insertImage(getContentResolver(), saveImageview, s, "descripton");
            Toast.makeText(getApplicationContext(), "꾸미기 완료", Toast.LENGTH_SHORT).show();
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