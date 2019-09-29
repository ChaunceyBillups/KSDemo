package com.example.ksdemo.activity;

import static com.example.ksdemo.utils.Constants.GROUP_NAME;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import com.example.ksdemo.App;
import com.example.ksdemo.R;
import com.example.ksdemo.camera.CameraView;
import com.example.ksdemo.db.EmployeeDBTool;
import com.example.ksdemo.model.Employee;
import com.example.ksdemo.utils.Constants;
import com.example.ksdemo.utils.FaceConfig;
import com.example.ksdemo.utils.Logger;
import com.example.ksdemo.utils.Systems;
import com.example.ksdemo.utils.TaskUtils;
import com.example.ksdemo.widget.CommonDialog;
import com.example.ksdemo.widget.FaceViewKS;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.SystemClock;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import megvii.facepass.FacePassException;
import megvii.facepass.types.FacePassDetectionResult;
import megvii.facepass.types.FacePassFace;
import megvii.facepass.types.FacePassImage;
import megvii.facepass.types.FacePassRecognitionResult;
import megvii.facepass.types.FacePassRecognitionResultType;
import megvii.facepass.types.FacePassRect;

/**
 * 刷脸界面
 */
public class MainActivity extends AppCompatActivity {

    private CameraView mCameraView;
    // the thread used to track
    private Thread mTrackThread;
    // 识别线程
    private Thread mRecognizeThread;
    private ArrayBlockingQueue<FacePassDetectionResult> mDetectResultQueue =
            new ArrayBlockingQueue<>(2);

    // the nv21 data is already copied
    private volatile boolean mIsNv21DataReady;
    private volatile boolean mIsTracking;
    // is kill track thread
    private boolean mIsKill;
    // the nv21 data
    private byte[] mNv21;

    private View bottomView;
    private TextView powerdText;
    private TextView companyText;
    private ScaleAnimation animation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f);
    private volatile boolean isAnimation = false;

    private FaceViewKS faceViewKS;

    // 0表示不旋转，1旋转90，2旋转180 3旋转270 正常手机是不用转的，qz的设备需要旋180
    private int cameraStreamRatation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        if (FaceConfig.DEVICE_TYPE == Constants.DEVICETYPE_QZ) {
            cameraStreamRatation = 2;
        } else if (FaceConfig.DEVICE_TYPE == Constants.DEVICETYPE_PHONE) {
            cameraStreamRatation = 0;
        }
        mCameraView = (CameraView) findViewById(R.id.camera_view);
        faceViewKS = findViewById(R.id.faceview_ks);

        mCameraView.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                mCameraView.addCallbackBuffer();
                if (mNv21 == null || data.length != mNv21.length) {
                    mNv21 = new byte[data.length];
                }
                onPreviewCallback(data, camera);
            }
        });
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mCameraView.minHeight = displayMetrics.heightPixels;
        mCameraView.minWidth = displayMetrics.widthPixels;
        powerdText = findViewById(R.id.powerd);

        companyText = findViewById(R.id.tv_title);
        bottomView = findViewById(R.id.bottom_layout);
        bottomView.setOnClickListener(new View.OnClickListener() {
            final static int COUNTS = 3; //点击次数
            final static long DURATION = 1 * 1000; //规定有效时间
            long[] mHits = new long[COUNTS];

            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                // 实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                    Toast.makeText(MainActivity.this, "进入调试模式", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, DebugActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        doOnResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        doOnPause();
    }

    /**
     * onResume时启动追踪分析线程
     */
    private void doOnResume() {
        mIsKill = false;
        // 追踪线程，负责检测有无人脸
        mTrackThread = new Thread() {
            @Override
            public void run() {
                while (!mIsKill && !Thread.interrupted()) {
                    if (mIsNv21DataReady) {
                        mIsTracking = true;
                        synchronized(mNv21) {
                            if (App.getInstance().mFacePassHandler == null) {
                                showFacePassFace(null);
                                continue;
                            }
                            if (FaceConfig.DEVICE_TYPE == Constants.DEVICETYPE_QZ) {
                                mNv21 = nv21Roate(mNv21, mCameraView.mPreviewWidth,
                                        mCameraView.mPreviewHeight);
                            }

                            FacePassImage image = null;
                            try {
                                image = new FacePassImage(mNv21, mCameraView.mPreviewWidth,
                                        mCameraView.mPreviewHeight, mCameraView.mDegrees, 0);
                            } catch (FacePassException e) {
                                e.printStackTrace();
                            }

                            try {
                                final FacePassDetectionResult detectionResult =
                                        App.getInstance().mFacePassHandler.feedFrame(image);
                                // 当前帧检测出人脸
                                if (detectionResult != null && detectionResult.faceList.length != 0) {
                                    /* 当前帧有检出人脸 */
                                    if (!App.getInstance().isPauseRecognize) {
                                        mDetectResultQueue.offer(detectionResult);
                                    }
                                    TaskUtils.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showFacePassFace(detectionResult);
                                        }
                                    });
                                } else {
                                    TaskUtils.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showFacePassFace(null);
                                        }
                                    });
                                }
                            } catch (FacePassException e) {
                                e.printStackTrace();
                            }

                            mIsTracking = false;
                            mIsNv21DataReady = false;
                        }
                    } else {
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            }
        };

        // 识别线程，负责识别具体的人员信息
        mRecognizeThread = new Thread() {
            @Override
            public void run() {
                while (!mIsKill && !Thread.interrupted()) {
                    if (App.getInstance().mFacePassHandler == null || App.getInstance().isPauseRecognize ) {
                        continue;
                    }
                    FacePassDetectionResult detectionResult = null;
                    try {
                        detectionResult = mDetectResultQueue.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (detectionResult == null) {
                        continue;
                    }

                    long start = System.currentTimeMillis();
                    if (detectionResult.message.length == 0) {
                        continue;
                    }
                    try {
                        Employee employee = null;
                        FacePassRecognitionResult[] recognizeResult =
                                App.getInstance().mFacePassHandler.recognize(GROUP_NAME,
                                        detectionResult.message);
                        for (FacePassRecognitionResult result : recognizeResult) {
                            String faceToken = new String(result.faceToken);
                            if (FacePassRecognitionResultType.RECOG_OK == result.facePassRecognitionResultType) {
                                employee = getEmployeeByFaceToken(result.trackId, faceToken);
                                long end = System.currentTimeMillis();
                                long total = end - start;
                                if (employee == null) {
                                    Logger.d("检索完毕 识别出来陌生人,耗时:" + total);
                                } else {
                                    Logger.d("检索完毕 识别出来" + employee.getName() + ",耗时:" + total);
                                    App.getInstance().isPauseRecognize = true;
                                    faceViewKS.setName(employee.getName());
                                    TaskUtils.handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            faceViewKS.setName("");
                                            App.getInstance().isPauseRecognize = false;
                                        }
                                    }, 2000);
                                }
                                break;
                            }

                        }
                    } catch (FacePassException e) {
                        e.printStackTrace();
                        // 陌生人也会报recognize failed异常
                        Logger.d("exception 识别出来陌生人");
                    }
                    // 能走到这一步，不为null说明是数据库有人，否则是陌生人，如果需要传识别记录照片，取检测时对应的NV21数据即可
                    // 重置handler，避免识别出来一次人脸之后ksResult.message.length一直为0的问题
                    App.getInstance().mFacePassHandler.reset();
                }
            }
        };

        mRecognizeThread.start();
        mTrackThread.start();
    }

    /**
     * onPause时停止追踪和分析
     */
    private void doOnPause() {
        mIsKill = true;
        if (mTrackThread != null) {
            try {
                mTrackThread.interrupt();
                mTrackThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (mRecognizeThread != null) {
            try {
                mRecognizeThread.interrupt();
                mRecognizeThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onPreviewCallback(byte[] data, Camera camera) {
        if (!mIsTracking) {
            synchronized(mNv21) {
                System.arraycopy(data, 0, mNv21, 0, data.length);
            }
            mIsNv21DataReady = true;
        }
    }

    @Override
    public void onBackPressed() {
        CommonDialog exitDialog = new CommonDialog(this, "提示", "确定退出程序吗", "取消",
                "确定", new CommonDialog.BtnClickListener() {
            @Override
            public void onNegativeClick() {
            }

            @Override
            public void onPositiveClick() {
                finish();
                System.exit(0);
            }
        });
        exitDialog.show();
    }

    private void showFacePassFace(FacePassDetectionResult detectionResult) {
        // 取出最大的face
        FacePassFace maxFace = null;
        if (detectionResult == null || detectionResult.faceList.length == 0) {
            faceViewKS.setFaces(maxFace);
            return;
        }

        for (int i = 0; i < detectionResult.faceList.length; i++) {
            if (maxFace == null ||
                    detectionResult.faceList[i].rect.right - detectionResult.faceList[i].rect.left
                            > maxFace.rect.right - maxFace.rect.left) {
                maxFace = detectionResult.faceList[i];
            }
        }

        boolean mirror = true; /* 前摄像头时mirror为true */
        if (FaceConfig.DEVICE_TYPE == Constants.DEVICETYPE_QZ) {
            mirror = false;
        }
        Matrix mat = new Matrix();
        int w = mCameraView.getMeasuredWidth();
        int h = mCameraView.getMeasuredHeight();

        int cameraHeight = 720;
        int cameraWidth = 1080 - 150;

        float left = 0;
        float top = 0;
        float right = 0;
        float bottom = 0;
        switch (mCameraView.mDegrees) {
            case 0:
                left = maxFace.rect.left;
                top = maxFace.rect.top;
                right = maxFace.rect.right;
                bottom = maxFace.rect.bottom;
                mat.setScale(mirror ? -1 : 1, 1);
                mat.postTranslate(mirror ? (float) cameraWidth : 0f, 0f);
                mat.postScale((float) w / (float) cameraWidth, (float) h / (float) cameraHeight);
                break;
            case 90:
                mat.setScale(mirror ? -1 : 1, 1);
                mat.postTranslate(mirror ? (float) cameraHeight : 0f, 0f);
                mat.postScale((float) w / (float) cameraHeight, (float) h / (float) cameraWidth);
                left = maxFace.rect.top;
                top = cameraWidth - maxFace.rect.right;
                right = maxFace.rect.bottom;
                bottom = cameraWidth - maxFace.rect.left;
                break;
            case 180:
                mat.setScale(1, mirror ? -1 : 1);
                mat.postTranslate(0f, mirror ? (float) cameraHeight : 0f);
                mat.postScale((float) w / (float) cameraWidth, (float) h / (float) cameraHeight);
                left = maxFace.rect.right;
                top = maxFace.rect.bottom;
                right = maxFace.rect.left;
                bottom = maxFace.rect.top;
                break;
            case 270:
                mat.setScale(mirror ? -1 : 1, 1);
                mat.postTranslate(mirror ? (float) cameraHeight : 0f, 0f);
                mat.postScale((float) w / (float) cameraHeight, (float) h / (float) cameraWidth);
                left = cameraHeight - maxFace.rect.bottom;
                top = maxFace.rect.left;
                right = cameraHeight - maxFace.rect.top;
                bottom = maxFace.rect.right;
        }

        // Logger.d("rect", left + "--" + top + "--" + right + "--" + bottom);

        RectF drect = new RectF();
        RectF srect = new RectF(left, top, right, bottom);
        mat.mapRect(drect, srect);

        maxFace.rect = new FacePassRect((int) drect.left, (int) drect.top,
                (int) (drect.left + drect.bottom - drect.top),
                (int) drect.bottom);
        faceViewKS.setFaces(maxFace);
    }

    private byte[] nv21Roate(byte[] nv21_data, int width, int height) {
        switch (cameraStreamRatation) {
            case 0:
                return nv21_data;
            case 1:
                return Systems.NV21_rotate_to_90(nv21_data, width, height);
            case 2:
                return Systems.NV21_rotate_to_180(nv21_data, width, height);
            case 3:
                return Systems.NV21_rotate_to_270(nv21_data, width, height);

        }
        return nv21_data;
    }


    private Employee getEmployeeByFaceToken(long trackId, String faceToken) {
        if (TextUtils.isEmpty(faceToken)) {
            return null;
        }

        List<Employee> employees = EmployeeDBTool.getInstance().getEmployeeList();
        if (employees == null || employees.size() == 0) {
            return null;
        }
        Employee employee = null;
        for (int i = 0; i < employees.size(); i++) {
            Employee tmp = employees.get(i);
            if (faceToken.equals(tmp.getFeature())) {
                employee = tmp;
                break;
            }
        }
        return employee;
    }
}
