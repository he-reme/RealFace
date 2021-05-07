#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/asset_manager_jni.h>
#include <android/log.h>


using namespace cv;
using namespace std;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_h_RealFace_ImageProcessingActivity_loadImage(JNIEnv *env, jobject instance,
                                                              jstring file,
                                                               jlong img) {
 //이미지 불러오는 함수
    const char *imageFileName = env->GetStringUTFChars(file, 0);
    // TODO

    Mat &img_input = *(Mat *) img;

    img_input = imread(imageFileName, IMREAD_COLOR); //이미지 파일 img_input Mat 형식으로 변환
    cvtColor( img_input, img_input, CV_BGR2RGB); //opencv는 파일을 BGR순서로 읽으므로 RGB로 변환하는 함수
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_h_RealFace_ImageProcessingActivity_tmpImage(JNIEnv *env, jobject instance,
                                                             jlong tmpImage, jlong outputImage) {
    //이미지 임시 저장 함수
    // TODO
    Mat &img_tmp = *(Mat *)tmpImage;
    Mat &img_output = *(Mat *) outputImage;

    img_output.copyTo(img_tmp);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_h_RealFace_ImageProcessingActivity_imageprocessing(JNIEnv *env, jobject instance,
                                                                    jlong inputImage,
                                                                    jlong outputImage, jint count) {
    //이미지 프로세싱 함수
    // TODO
    Mat &img_input = *(Mat *) inputImage;
    Mat &img_output = *(Mat *) outputImage;

    if(count==1){
        //스케치 필터
        img_input.copyTo(img_output);
        detailEnhance(img_output, img_output, 10, 0.15f); //opencv의 스케치 필터 함수
    }
    else if(count==2){
        //뽀샤시 필터
        img_input.copyTo(img_output);
        edgePreservingFilter(img_output, img_output,  1, 60,  0.4f); //opencv의 뽀샤시 필터 함수
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_h_RealFace_ImageProcessingActivity_unimageprocessing(JNIEnv *env, jobject instance,
                                                                      jlong inputImage,
                                                                      jlong outputImage,
                                                                      jlong tmpImage) {
    //원본 이미지로 돌리는 함수

    // TODO
    Mat &img_input = *(Mat *) inputImage;
    Mat &img_output = *(Mat *) outputImage;
    Mat &img_tmp = *(Mat *) tmpImage;


    img_input.copyTo(img_output);
    img_input.copyTo(img_tmp);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_h_RealFace_ImageProcessingActivity_imageBrightness(JNIEnv *env, jobject instance,
                                                                    jlong tmpImage,
                                                                    jlong outputImage, jint num) {
    //이미지 밝기 조절 함수

    // TODO
    Mat &img_tmp = *(Mat *) tmpImage;
    Mat &img_output = *(Mat *) outputImage;

    img_tmp.copyTo(img_output);
    img_output = img_output + Scalar(num,num,num);
    //필터 적용을 유지하기 위해 임시 저장 이미지(필터를 유지하고 있는)에 scalar값을 더하여 밝기 조절
}
