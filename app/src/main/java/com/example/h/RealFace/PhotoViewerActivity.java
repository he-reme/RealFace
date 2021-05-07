package com.example.h.RealFace;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PhotoViewerActivity extends Activity implements View.OnClickListener{

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    private static final String TAG = "PhotoViewerActivity";
    ImageButton galleryButton;
    ImageView first;
    Bitmap tmp;

    String filename;

    static final int getimagesetting=1001;//for request intent

    static final int getGallery=2002;
    int count = 0;

    private String[] permissions = {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
            ,Manifest.permission.READ_EXTERNAL_STORAGE}; //권한 설정 변수

    private static final int MULTIPLE_PERMISSIONS = 101;
    //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        if(checkPermissions())
            init();

        final ImageButton chooseButton = (ImageButton)findViewById(R.id.chooseButton);
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count == 1) {
                    save();
                    Intent intent = new Intent(PhotoViewerActivity.this, ImageProcessingActivity.class);

                    intent.putExtra("filename",filename);
                    startActivity(intent);
                }
                else
                    Toast.makeText(getApplicationContext(), "사진첩을 통해 사진을 선택해 주세요", Toast.LENGTH_LONG).show();
            }
        });

    }

    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        Log.e(this.getClass().getName(),"checkPermissions");
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) { //권한이 추가되었으면 해당 리스트가 empty가 아니므로 request 즉 권한을 요청함.
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        //권한 요청 응답 처리
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    //요청이 취소되면 결과 배열이 비어있게 됨
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                                break;
                            }
                        }
                        if(i==permissions.length-1)
                            init();
                    }
                } else {
                    showNoPermissionToastAndFinish();
                }
            }
        }
    }
    private void showNoPermissionToastAndFinish() {
        //권한 획득에 동의 하지 않았을 경우 아래 Toast 메세지 띄움.
        Toast.makeText(this,getString(R.string.limit),Toast.LENGTH_SHORT).show();
        init();
    }

    void init(){
        galleryButton=findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent();
        switch (v.getId()){
            case R.id.galleryButton: //내부 저장공간에서 사진 파일 가져오기
                count = 0;
                intent.setAction(Intent.ACTION_GET_CONTENT); //항상 선택 도구 표시
                intent.setType("image/*"); //이미지형식만 보여준다
                startActivityForResult(intent, getGallery);
                break;
        }
    }

    void run(Bitmap image){
        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                //빌더를 사용하여 얼굴을 검출할 수 있는 새로운 FaceDetector 객체 생성
                .setTrackingEnabled(false) //추적 활성화를 false 로 설정
                .setLandmarkType(FaceDetector.ALL_LANDMARKS) //랜드마크를 시각화
                .build();

        Detector<Face> safeDetector = new SafeFaceDetector(detector);

        Frame frame = new Frame.Builder().setBitmap(image).build();
        //비트맵이 주어지면 비트맵에서 frame 인스턴스 생성하여 감지기에 공급
        SparseArray<Face> faces = safeDetector.detect(frame);
        //비트맵을 사용하여 프레임 만든 후 이 프레임을 사용하여
        //FaceDetector 에서 detect 메소드 호출하여 Face 객체의 SparseArray 가져옴

        if (!safeDetector.isOperational()) { //필요한 네이티브 라이브러리가 현재 사용 가능한지 확인

            Log.w(TAG, "Face detector dependencies are not yet available.");

            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            //낮은 저장 공간 확인하기. 저장소가 부족하면 기본 라이브러리가 다운로드 되지 않으므로 검출이 작동하지 않는다.

            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        FaceView overlay = (FaceView) findViewById(R.id.faceView);

        first = (ImageView)findViewById(R.id.first);
        first.setImageResource(R.drawable.checkface);

        tmp = overlay.setContent(image, faces);

        safeDetector.release();
        //감지기는 여러 이미지에 여러 번 사용할 수 있지만
        // 원시 리소스를 확보하기 위해 더 이상 필요하지 않은 경우에는 릴리스해야 함.

}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bm=null;
        if(resultCode==RESULT_OK){
            switch(requestCode){
                case getGallery: //갤러리에서 사진을 가져오면 onActivityResult 호출
                    try {
                        bm = MediaStore.Images.Media.getBitmap( getContentResolver(), data.getData());
                        count =1;
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch(OutOfMemoryError e){
                        Toast.makeText(getApplicationContext(), "이미지 용량이 너무 큽니다.", Toast.LENGTH_SHORT).show();
                    }
                    run(bm);//얼굴인식
                default:
                    break;
            }
        }
    }

    private void save(){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        final FaceView saveImage = (FaceView) findViewById(R.id.faceView);//캡쳐할영역(프레임레이아웃)
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
            Toast.makeText(getApplicationContext(), "폴더가 생성되었습니다.", Toast.LENGTH_SHORT).show();
        }

        SimpleDateFormat day = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        saveImage.buildDrawingCache();
        Bitmap saveImageview = saveImage.getDrawingCache( );

        String s = day.format(date);
        FileOutputStream fos = null;
        filename = s+".png";
        try {
            fos = new FileOutputStream(path + "/" + s + ".png");
            saveImageview.compress(Bitmap.CompressFormat.PNG, 100, fos);
    //        MediaStore.Images.Media.insertImage(getContentResolver(), saveImageview, s, "descripton");
            Toast.makeText(getApplicationContext(), "사진선택 완료", Toast.LENGTH_SHORT).show();
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



