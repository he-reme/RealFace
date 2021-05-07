package com.example.h.RealFace;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import com.google.android.gms.vision.face.Face;

public class FaceView extends View {
    private Bitmap mBitmap;
    private SparseArray<Face> mFaces;
    private Bitmap tmp;

    public FaceView(Context context){
        super(context,null);
    }

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FaceView(Context context, AttributeSet attrs,int defStyleAttr){
        super(context,attrs,defStyleAttr);
    }

    Bitmap setContent(Bitmap bitmap, SparseArray<Face> faces) {
        mBitmap = bitmap;

        mFaces = faces;
        invalidate();
        return tmp;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //자동으로 불림
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE); //흰색 캔버스 설정


        if ((mBitmap != null) && (mFaces != null)) {
            // drawBitmap(canvas);//비트맵으로 이미지를 불러와 캔버스에 drawbitmap

            Bitmap roundBitmap = drawBitmap(mBitmap);
            canvas.drawBitmap(roundBitmap,0,0,null);
        }
    }

    private Bitmap drawBitmap(Bitmap mBitmap) {

        int size = 500; //크기 설정
        float left = 0;
        float top = 0;
        float right = 0;
        float bottom = 0;

        for (int i = 0; i < mFaces.size(); i++) {
            Face face = mFaces.valueAt(i);
            left = (float) (face.getPosition().x);
            top = (float) (face.getPosition().y);
            right = (float) (face.getPosition().x + face.getWidth());
            bottom = (float) (face.getPosition().y + face.getHeight());
        } //각 얼굴에 대한 face 배열에서 위치를 추출
        Bitmap output = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom); //(left,top)에서 (right,bottom)까지 사각형 src를 지정
        final Rect dst = new Rect(0, 0, size, size); //(0,0)에서 (500,500)까지 사각형 dst지정

        paint.setAntiAlias(true); //표면처리를 부드럽게 한다.
        paint.setFilterBitmap(true); //페인트의 플래그에 FILTER_BITMAP_FLAG 비트 설정
        paint.setDither(true); //DITHER_FLAG 비트를 설정 또는 삭제하는 도우미에 영향을 줌.

        canvas.drawARGB(0, 0, 0, 0); //색을 칠하는 함수
        paint.setColor(Color.parseColor("#FFFFFF")); //원의 색상을 하얀색으로 지정
        canvas.drawCircle(250, 250+50, 170, paint); //원의 줌심은 (250,250)이며 반지름은 170이다.
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN)); //원과 비트맵의 교집합 부분
        canvas.drawBitmap(mBitmap, src, dst, paint); //비트맵의 src영역을 캔버스의 dst영역에 출력한다.
        tmp = output;
        return output;
    }
}
