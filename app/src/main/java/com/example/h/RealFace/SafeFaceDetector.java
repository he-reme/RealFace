package com.example.h.RealFace;

import android.graphics.ImageFormat;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.nio.ByteBuffer;
import java.util.Arrays;


public class SafeFaceDetector extends Detector<Face> {
    // 얼굴 검출기의 버그에 대한 해결책
    // 이 버그는 매우 작은 이미지(즉, 치수가 147인 대부분의 이미지)와 매우 얇은 이미지 중 하나가
    // 네이티브 얼굴 감지 코드에 충돌을 일으킬 수 있음.
    // 이 문제를 피하기 위해 얼굴 감지 전에 그러한 이미지에 패딩을 추가함
    private static final String TAG = "SafeFaceDetector";
    private Detector<Face> mDelegate;

    //얼굴 검출기 버그를 유발하는 이미지로부터 보호하는 얼굴 검출기를 만듬

    public SafeFaceDetector(Detector<Face> delegate) {
        mDelegate = delegate;
    }

    @Override
    public void release() {
        mDelegate.release();
    }

    /* 제공된 이미지가 기본 안면 검출기에 문제를 일으킬 수 있는지 여부를 결정
       이 경우 문제가 발생하지 않도록 패딩을 이미지에 추가.*/
    @Override
    public SparseArray<Face> detect(Frame frame) {
        final int kMinDimension = 147;
        final int kDimensionLower = 640;
        int width = frame.getMetadata().getWidth();
        int height = frame.getMetadata().getHeight();

        if (height > (2 * kDimensionLower)) {
            //감지가 실행되기 전에 이미지의 크기를 줄인다.
            // 너비가 최소값 이하로 떨어지지 않는지 확인해야 함.
            double multiple = (double) height / (double) kDimensionLower;
            double lowerWidth = Math.floor((double) width / multiple);
            if (lowerWidth < kMinDimension) {
                //다운샘플링 시 너비가 최소치 이하로 떨어졌으므로
                //패딩을 우측에 적용하여 너비를 충분히 크게 유지해야 함.
                int newWidth = (int) Math.ceil(kMinDimension * multiple);
                frame = padFrameRight(frame, newWidth);
            }
        } else if (width > (2 * kDimensionLower)) {
            //감지가 실행되기 전에 이미지의 크기를 줄인다.
            // 높이가 최소값보다 낮아지지 않는지 확인해야 함
            double multiple = (double) width / (double) kDimensionLower;
            double lowerHeight = Math.floor((double) height / multiple);
            if (lowerHeight < kMinDimension) {
                int newHeight = (int) Math.ceil(kMinDimension * multiple);
                frame = padFrameBottom(frame, newHeight);
            }
        } else if (width < kMinDimension) {
            frame = padFrameRight(frame, kMinDimension);
        }

        return mDelegate.detect(frame);
    }

    @Override
    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    @Override
    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }


    //오른쪽의 추가 폭을 사용하여 원래 프레임을 기준으로 새 프레임을 재정렬
    //하부 얼굴 검출기의 버그를 방지하기 위해 크기를 증가시킴.
    private Frame padFrameRight(Frame originalFrame, int newWidth) {
        Frame.Metadata metadata = originalFrame.getMetadata();
        int width = metadata.getWidth();
        int height = metadata.getHeight();

        Log.i(TAG, "Padded image from: " + width + "x" + height + " to " + newWidth + "x" + height);

        ByteBuffer origBuffer = originalFrame.getGrayscaleImageData();
        int origOffset = origBuffer.arrayOffset();
        byte[] origBytes = origBuffer.array();

        // 프레임이 비직접 바이트 버퍼를 지원할 때 나중에 할당하도록 변경 가능
        ByteBuffer paddedBuffer = ByteBuffer.allocateDirect(newWidth * height);
        int paddedOffset = paddedBuffer.arrayOffset();
        byte[] paddedBytes = paddedBuffer.array();
        Arrays.fill(paddedBytes, (byte) 0);

        for (int y = 0; y < height; ++y) {
            int origStride = origOffset + y * width;
            int paddedStride = paddedOffset + y * newWidth;
            System.arraycopy(origBytes, origStride, paddedBytes, paddedStride, width);
        }

        return new Frame.Builder()
                .setImageData(paddedBuffer, newWidth, height, ImageFormat.NV21)
                .setId(metadata.getId())
                .setRotation(metadata.getRotation())
                .setTimestampMillis(metadata.getTimestampMillis())
                .build();
    }

    //밑면 검출기의 버그를 방지하기 위해 크기를 증가시키기 위해
    // 바닥의 추가 높이를 사용하여 원래 프레임을 기반으로 새 프레임을 작성
    private Frame padFrameBottom(Frame originalFrame, int newHeight) {
        Frame.Metadata metadata = originalFrame.getMetadata();
        int width = metadata.getWidth();
        int height = metadata.getHeight();

        Log.i(TAG, "Padded image from: " + width + "x" + height + " to " + width + "x" + newHeight);

        ByteBuffer origBuffer = originalFrame.getGrayscaleImageData();
        int origOffset = origBuffer.arrayOffset();
        byte[] origBytes = origBuffer.array();

        // 프레임이 비직접 바이트 버퍼를 지원할 때 나중에 할당하도록 변경 가능
        ByteBuffer paddedBuffer = ByteBuffer.allocateDirect(width * newHeight);
        int paddedOffset = paddedBuffer.arrayOffset();
        byte[] paddedBytes = paddedBuffer.array();
        Arrays.fill(paddedBytes, (byte) 0);

        //패딩 처리된 하단 부분을 채우지 않고 원본에서 영상 내용을 복사
        for (int y = 0; y < height; ++y) {
            int origStride = origOffset + y * width;
            int paddedStride = paddedOffset + y * width;
            System.arraycopy(origBytes, origStride, paddedBytes, paddedStride, width);
        }

        return new Frame.Builder()
                .setImageData(paddedBuffer, width, newHeight, ImageFormat.NV21)
                .setId(metadata.getId())
                .setRotation(metadata.getRotation())
                .setTimestampMillis(metadata.getTimestampMillis())
                .build();
    }
}