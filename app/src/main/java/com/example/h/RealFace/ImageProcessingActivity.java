package com.example.h.RealFace;
//지금 내가 하고있는 것은 bitmap인 파일을 받아왔는데 어케해야할지

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageProcessingActivity extends AppCompatActivity {
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    ImageView imageVIewOuput; //수정할 이미지
    SeekBar seekBar; //밝기 조절 바
    ImageButton processingButton; //필터 버튼
    ImageButton chooseButton; //결정 버튼
    ImageButton retryButton; //재시도 버튼, 전 액티비티로 돌아감

    int num; //seekBar 현재 값 저장
    int count; //필터 입히기 위한 제한장치

    private Mat img_input; //input 이미지
    private Mat img_output; //output 이미지
    private Mat img_tmp; //이미지 임시저장
    private String filename; //파일 이름(name.png)

    private static final String TAG = "opencv";
    static final int PERMISSION_REQUEST_CODE = 1;
    String[] PERMISSIONS  = {"android.permission.WRITE_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageprocessing);
        count = 1;

        Intent intent =getIntent();//filename 전 액티비티에서 받아옴
        filename = intent.getExtras().getString("filename");

        //이미지 불러옴
        imageVIewOuput = (ImageView)findViewById(R.id.imageViewOutput);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        processingButton = (ImageButton)findViewById(R.id.processingButton);
        chooseButton = (ImageButton)findViewById(R.id.chooseButton);
        retryButton = (ImageButton)findViewById(R.id.retryButton);

        if (!hasPermissions(PERMISSIONS)) { //퍼미션 허가를 했었는지 여부를 확인
            requestNecessaryPermissions(PERMISSIONS);//퍼미션 허가안되어 있다면 사용자에게 요청
        } else {
            //이미 사용자에게 퍼미션 허가를 받음.

            read_image_file(); //이미지 파일 읽음

            unimageprocess_and_showResult(); //필터 해제

            Bitmap bitmapinput = Bitmap.createBitmap(img_input.cols(), img_input.rows(), Bitmap.Config.ARGB_8888); //input을 넣을 비트맵 생성

            Utils.matToBitmap(img_input, bitmapinput); // mat을 bitmap으로 변환
            imageVIewOuput.setImageBitmap(bitmapinput); //액티비티에 이미지 보이게 함


            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                //밝기 조절 바
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if(i<0)
                    { //밝기 크기가 음수가 되면 0으로 지정
                        i = 0;
                    }else if(i>100)
                    { //밝기 크기가 100넘어가면 100으로 지정(밝기 값이 너무 커서 이미지 보이지 않는 현상 막기 위해)

                        i=100;
                    }
                    seekBar.setProgress(i); //seekBar의 i값으로 설정
                    num=i; //밝기값 적용하기 위한 변수
                    imageBrightness_and_showResult(num); // 밝기 조절 함수
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            processingButton.setOnClickListener(new ImageButton.OnClickListener(){
                //필터 버튼 클릭 시
                android.app.AlertDialog alert = resetAlertDialog();
                @Override
                public void onClick(View view){
                    alert.show();
                }
            });
        }

        chooseButton.setOnClickListener(new View.OnClickListener() {
            //사진 프로세싱 후 결정 버튼 클릭 시
            @Override
            public void onClick(View view) {
                save();
                Intent intent = new Intent(ImageProcessingActivity.this, MakeCharacterActivity.class);
                intent.putExtra("filename",filename);
                startActivity(intent);
            }
        });

        retryButton.setOnClickListener(new View.OnClickListener() {
            //재시도 버튼으로, 전 액티비티로 되돌아감
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ImageProcessingActivity.this, PhotoViewerActivity.class);
                startActivity(intent);
            }
        });
    }

    public android.app.AlertDialog resetAlertDialog()
    {
        //필터 적용
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("필터")
                .setMessage("※확인을 누르면 밝기가 초기화 됩니다")
                .setCancelable(false)
                .setPositiveButton("확인",
                        //확인 눌렀을 때
                        new DialogInterface.OnClickListener() {
                            @Override
                            //스케치->뽀샤시->지우기 순서로 반복
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (count ==1) {
                                    //스케치 필터 적용.  count 값 1은 sketch필터
                                    seekBar.setProgress(0);
                                    imageprocess_and_showResult(count); //이미지 프로세싱 함수
                                    processingButton.setImageResource(R.drawable.phoshop); //다음 기능인 뽀샤시 버튼으로 바꿈
                                    Toast.makeText(getApplicationContext(), "스케치 적용: ", Toast.LENGTH_LONG).show();
                                    count=2;
                                }
                                else if(count==2){
                                    //뽀샤시 필터 적용. count 값 2는 phoshop필터
                                    seekBar.setProgress(0);

                                    imageprocess_and_showResult(count); //이미지 프로세싱 함수
                                    processingButton.setImageResource(R.drawable.erase); //다음 기능인 (필터) 지우기 버튼으로 바꿈
                                    Toast.makeText(getApplicationContext(), "뽀샤시 적용: ", Toast.LENGTH_LONG).show();
                                    count = 3;
                                }
                                else if(count==3){
                                    //count가 1, 2 아닌 값일 때는 필터 지우기
                                    seekBar.setProgress(0);
                                    unimageprocess_and_showResult(); //원본 이미지로 바꾸는 함수
                                    processingButton.setImageResource(R.drawable.sketch); //다음 기능인 스케치 버튼으로 바꿈
                                    Toast.makeText(getApplicationContext(), "필터 지우기", Toast.LENGTH_LONG).show();
                                    count=1;
                                }

                                tmpImage(img_tmp.getNativeObjAddr(), img_output.getNativeObjAddr());
                            }
                        })
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_LONG).show();
                            }
                        });
        android.app.AlertDialog alert = builder.create();
        return alert;
    }


    private boolean hasPermissions(String[] permissions) {
        int ret = 0;
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){
            ret = checkCallingOrSelfPermission(perms);
            if (!(ret == PackageManager.PERMISSION_GRANTED)){
                //퍼미션 허가 안된 경우
                return false;
            }
        }
        //모든 퍼미션이 허가된 경우
        return true;
    }

    private void requestNecessaryPermissions(String[] permissions) {
        //마시멜로( API 23 )이상에서 런타임 퍼미션(Runtime Permission) 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        //퍼미션 결과 반환
        switch(permsRequestCode){

            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if (!writeAccepted )
                        {
                            showDialogforPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                            return;
                        }else
                        {
                            read_image_file();
                        }
                    }
                }
                break;
        }
    }

    private void showDialogforPermission(String msg) {
        //퍼미션 받기 위한 알림
        final AlertDialog.Builder myDialog = new AlertDialog.Builder(ImageProcessingActivity.this);
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
                }

            }
        });
        myDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        myDialog.show();
    }


    private void imageBrightness_and_showResult(int num){
        //이미지 밝기 조절 한 후 결과 값 이미지 반환
        imageBrightness(img_tmp.getNativeObjAddr(), img_output.getNativeObjAddr(),num);

        Bitmap bitmapOutput = Bitmap.createBitmap(img_output.cols(), img_output.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_output, bitmapOutput);
        imageVIewOuput.setImageBitmap(bitmapOutput);
    }

    private void imageprocess_and_showResult(int count) {
        //이미지 필터 적용 한 후 결과 값 이미지 변환
        imageprocessing(img_input.getNativeObjAddr(), img_output.getNativeObjAddr(),count);

        Bitmap bitmapOutput = Bitmap.createBitmap(img_output.cols(), img_output.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_output, bitmapOutput);
        imageVIewOuput.setImageBitmap(bitmapOutput);
    }

    private void unimageprocess_and_showResult() {
        //원본 이미지로 되돌린 후 결과 값 이미지 변환
        unimageprocessing(img_input.getNativeObjAddr(), img_output.getNativeObjAddr(), img_tmp.getNativeObjAddr());

        Bitmap bitmapOutput = Bitmap.createBitmap(img_output.cols(), img_output.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_output, bitmapOutput);
        imageVIewOuput.setImageBitmap(bitmapOutput);
    }


    private void copyFile(String filename) {
        //이미지 파일 카피
        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;

        AssetManager assetManager = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d( TAG, "copyFile :: 다음 경로로 파일복사 "+ pathDir);
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 "+e.toString() );
        }
    }

    private void read_image_file() {
        //이미지 파일 불러오기
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String file = path + File.separator + filename; //전 액티비티에서 받아온 파일 이름으로 디렉토리에서 불러옴
        copyFile(filename);
        img_input = new Mat();
        img_output = new Mat();
        img_tmp = new Mat();

        loadImage(file, img_input.getNativeObjAddr()); //이미지 파일 input파일에 불러옴
    }

    private void save(){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath(); //저장할 path
        final ImageView saveImage = (ImageView) findViewById(R.id.imageViewOutput);//캡쳐할영역(프레임레이아웃)

        File file = new File(path);
        if (!file.exists()) { //파일 존재하지 않으면
            file.mkdirs(); //폴더 생성
            Toast.makeText(getApplicationContext(), "폴더가 생성되었습니다.", Toast.LENGTH_SHORT).show();
        }

        SimpleDateFormat day = new SimpleDateFormat("yyyyMMddHHmmss"); //이미지 이름
        Date date = new Date();
        saveImage.buildDrawingCache();
        Bitmap saveImageview = saveImage.getDrawingCache();

        String s = day.format(date);
        FileOutputStream fos = null;
        filename = s+".png"; //다음 액티비티에 보낼 변수명
        try {
            fos = new FileOutputStream(path + "/" + s + ".png"); //이미지 파일 생성
            saveImageview.compress(Bitmap.CompressFormat.PNG, 100, fos); //비트맵 형식을 png 형식으로 바꾼다
            Toast.makeText(getApplicationContext(), "보정완료", Toast.LENGTH_SHORT).show();
            fos.flush();
            fos.close();
            saveImage.destroyDrawingCache();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //native-lib.cpp 파일
    public native void loadImage(String file, long img); //이미지 불러오는 cpp함수
    public native void tmpImage(long tmpImage, long outputImage); //이미지 임시 저장 함수
    public native void imageprocessing(long inputImage, long outputImage, int count); //이미지 프로세싱 함수
    public native void unimageprocessing(long inputImage, long outputImage, long tmpImage); //원본 이미지 돌려놓는 함수
    public native void imageBrightness(long tmpImage, long outputImage, int num); //이미지 밝기 조절 함수
}
