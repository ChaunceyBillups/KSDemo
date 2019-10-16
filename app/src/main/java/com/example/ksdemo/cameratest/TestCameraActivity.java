package com.example.ksdemo.cameratest;

import java.io.ByteArrayOutputStream;

import com.example.ksdemo.R;
import com.example.ksdemo.utils.Logger;
import com.example.ksdemo.utils.Systems;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 相机测试界面
 */
public class TestCameraActivity extends AppCompatActivity {

    private TestCameraView mCameraView;
    private ImageView streamImg;
    // 旋转的角度是按顺时针旋转的
    // 测试相机的预览旋转角度
    public static final int PREVIEW_DEGREE = 90;
    // 测试相机的输出流旋转角度
    public static final int STREAM_DEGREE = 0;

    private TextView cameraText;
    private TextView streamText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_testcamera);
        initView();
    }

    private void initView() {
        mCameraView = findViewById(R.id.test_camera_view);
        streamImg = findViewById(R.id.stream_img);
        mCameraView.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                mCameraView.addCallbackBuffer();
                Camera.Size size = camera.getParameters().getPreviewSize();
                try {
                    byte[] nv21Roate = nv21Roate(data, size.width, size.height);

                    YuvImage image = new YuvImage(nv21Roate, ImageFormat.NV21, size.width, size.height,
                                    null);
                    // 如果旋转角度是90度或者270度，长度和宽度需要交换
                    //  YuvImage image = new YuvImage(nv21Roate, ImageFormat.NV21, size.height,
                    //                  size.width, null);
                    if (image != null) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
                        // 如果旋转角度是90度或者270度，长度和宽度需要交换
                        // image.compressToJpeg(new Rect(0, 0, size.height, size.width), 80,stream);
                        Bitmap bmp = BitmapFactory
                                .decodeByteArray(stream.toByteArray(), 0, stream.size());
                        streamImg.setImageBitmap(bmp);
                        stream.close();
                    }
                } catch (Exception ex) {
                    Logger.d("Sys", "Error:" + ex.getMessage());
                }

            }
        });

        cameraText = findViewById(R.id.camera_text);
        cameraText.setText("相机预览当前旋转角度:" + PREVIEW_DEGREE);
        streamText = findViewById(R.id.stream_text);
        streamText.setText("相机输出流当前旋转角度:" + STREAM_DEGREE);
    }

    private byte[] nv21Roate(byte[] nv21_data, int width, int height) {
        switch (STREAM_DEGREE) {
            case 0:
                return nv21_data;
            case 90:
                return Systems.NV21_rotate_to_90(nv21_data, width, height);
            case 180:
                return Systems.NV21_rotate_to_180(nv21_data, width, height);
            case 270:
                return Systems.NV21_rotate_to_270(nv21_data, width, height);

        }
        return nv21_data;
    }

}
