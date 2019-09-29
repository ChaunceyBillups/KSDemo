package com.example.ksdemo.activity;

import static com.example.ksdemo.utils.Constants.GROUP_NAME;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.example.ksdemo.App;
import com.example.ksdemo.R;
import com.example.ksdemo.db.EmployeeDBTool;
import com.example.ksdemo.utils.Constants;
import com.example.ksdemo.utils.Logger;
import com.example.ksdemo.widget.CommonDialog;
import com.example.ksdemo.widget.LoadingDialog;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import megvii.facepass.FacePassException;
import megvii.facepass.FacePassHandler;
import megvii.facepass.types.FacePassConfig;
import megvii.facepass.types.FacePassImageRotation;
import megvii.facepass.types.FacePassModel;
import megvii.facepass.types.FacePassPose;

/**
 * codemaster
 */
public class WelcomeActivity extends BaseActivity {

    private static final String authLink = "https://api-cn.faceplusplus.com";
    private static final String ak = "";
    private static final String sk = "";
    private static final int INIT_REQUEST_PERMISSIONS = 1001;
    private static final int MSG_INIT_SUC = 1002;
    private LoopHandler loopHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        loopHandler = new LoopHandler(new WeakReference<Context>(this));
        handlePermission();
    }


    private void handlePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Logger.d("手机系统版大于等于AndroidM 先申请权限");
            ArrayList<String> permissions = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }

            // 说明权限都申请过,走正常的初始化逻辑
            if (permissions.size() == 0) {
                initSDK();
            } else {
                requestPermissions(permissions.toArray(new String[permissions.size()]), INIT_REQUEST_PERMISSIONS);
            }

        } else {
            Logger.d("手机系统版本小于AndroidM正常启动");
            initSDK();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Logger.d("WelcomeActivity收到权限通知:" + requestCode);
        // 本次要权限的目的就是一次性的申请需要的权限，就算此处不同意，也不影响初始化
        if (requestCode == INIT_REQUEST_PERMISSIONS) {
            boolean sdP = true;
            boolean cameraP = true;
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(permissions[i])) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        sdP = false;
                    }
                }else if (Manifest.permission.CAMERA.equals(permissions[i])) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        cameraP = false;
                    }
                }
            }
            // 授权OK，走正常初始化
            if (sdP && cameraP) {
                initSDK();
            } else {
                showPermissionDialog();
            }
        }
    }

    private void initSDK(){
        LoadingDialog.showLoading(WelcomeActivity.this, "初始化中...");
        FacePassHandler.getAuth(authLink, ak, sk);
        FacePassHandler.initSDK(App.getInstance());
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    Logger.d("" + FacePassHandler.isAuthorized());
                    while (FacePassHandler.isAvailable()) {
                        Logger.d("start to build FacePassHandler");
                        FacePassConfig config;
                        try {
                            /* 填入所需要的配置 */
                            config = new FacePassConfig();
                            config.poseModel = FacePassModel.initModel(App.getInstance().getAssets(), "pose.alfa.tiny.170515.bin");
                            config.blurModel =
                                    FacePassModel.initModel(App.getInstance().getAssets(),
                                            "blurness.v5.l2rsmall.bin");

                            config.livenessModel = FacePassModel.initModel(App.getInstance().getAssets(), "liveness.3288CPU.rgb.20190630.bin");

                            //也可以使用GPU活体模型，GPU活体模型分两个，用于GPU加速的模型和CACHE，当使用CPU活体模型时，请传null，当使用GPU活体模型时，必须传入加速cache模型
                            //                            config.livenessModel = FacePassModel.initModel(OpenAIApplication.getInstance().getAssets(), "liveness.3288GPU.rgb.20190630.bin");
                            //                            config.livenessGPUCache = FacePassModel.initModel(OpenAIApplication.getInstance().getAssets(), "liveness.3288GPU.rgb.20190630.cache");

                            config.searchModel = FacePassModel.initModel(App.getInstance().getAssets(), "feat.small.3288_255MFlops_int8_150ms.1core.20190625.bin");
                            config.detectModel = FacePassModel.initModel(App.getInstance().getAssets(), "detector.retinanet.x14.f2h.190630_int8.bin");
                            config.detectRectModel = FacePassModel.initModel(App.getInstance().getAssets(), "detector_rect.retinanet.x14.f2h.190630_int8.bin");
                            config.landmarkModel = FacePassModel.initModel(App.getInstance().getAssets(), "lmk.rect_score.vgg.12M.20190121_81.bin");

                            //                            config.smileModel = FacePassModel.initModel(OpenAIApplication.getInstance().getAssets(), "attr.blur.align.gray.general.mgf29.0.1.1.181229.bin");
                            //                            config.ageGenderModel = FacePassModel.initModel(OpenAIApplication.getInstance().getAssets(), "age_gender.v2.bin");
                            //如果不需要表情和年龄性别功能，smileModel和ageGenderModel可以为null
                            config.smileModel = null;
                            config.ageGenderModel = null;

                            config.searchThreshold = 70;
                            config.livenessThreshold = 80;
                            config.livenessEnabled = false;
                            // ageGenderEnabledGlobal = (config.ageGenderModel != null);
                            config.faceMinThreshold = 100;
                            config.poseThreshold = new FacePassPose(30f, 30f, 30f);
                            config.blurThreshold = 0.45f;
                            // config.blurThreshold = 0.2f;
                            config.lowBrightnessThreshold = 70f;
                            config.highBrightnessThreshold = 210f;
                            config.brightnessSTDThreshold = 60f;
                            config.retryCount = 2;
                            config.smileEnabled = false;
                            config.maxFaceEnabled = true;

                            int cameraRotation = 0;
                            int windowRotation = ((WindowManager) (App.getInstance().getSystemService(Context.WINDOW_SERVICE)))
                                    .getDefaultDisplay().getRotation() * 90;
                            if (windowRotation == 0) {
                                cameraRotation = FacePassImageRotation.DEG90;
                            } else if (windowRotation == 90) {
                                cameraRotation = FacePassImageRotation.DEG0;
                            } else if (windowRotation == 270) {
                                cameraRotation = FacePassImageRotation.DEG180;
                            } else {
                                cameraRotation = FacePassImageRotation.DEG270;
                            }
                            config.rotation = cameraRotation;

                            File file = new File(Constants.SDPATH);
                            if (!file.exists()) {
                                file.mkdirs();
                            }
                            config.fileRootPath = Constants.SDPATH;

                            /* 创建SDK实例 */
                            App.getInstance().mFacePassHandler = new FacePassHandler(config);
                            // 创建默认群组
                            if (App.getInstance().mFacePassHandler == null) {
                                LoadingDialog.hideLoading();
                                showMsgDialog(false, "KS SDK初始化失败");
                                return;
                            }

                            // 设置入库的参数开始 放到底 10，50，0.8，10，250，250时可以全部入库
                            // 设置入库的参数开始 正常参数 50，30，0.4，50，210，80时可以全部入库
                            FacePassConfig addFaceConfig = App.getInstance().mFacePassHandler.getAddFaceConfig();

                            addFaceConfig.faceMinThreshold = 10;
                            addFaceConfig.poseThreshold = new FacePassPose(50f, 50f, 50f);
                            // 模糊的，越大越容易通过
                            addFaceConfig.blurThreshold =  0.8f;
                            addFaceConfig.lowBrightnessThreshold = 10f;
                            addFaceConfig.highBrightnessThreshold = 250f;
                            addFaceConfig.brightnessSTDThreshold = 250f;
                            // addFaceConfig.lowBrightnessThreshold = 50;
                            // addFaceConfig.highBrightnessThreshold = 300;
                            // addFaceConfig.blurThreshold = 0.4f;
                            App.getInstance().mFacePassHandler.setAddFaceConfig(addFaceConfig);
                            // 设置入库的参数结束

                            String[] localGroups = App.getInstance().mFacePassHandler.getLocalGroups();
                            if (localGroups == null || localGroups.length == 0) {
                                boolean isSuccess = false;
                                try {
                                    isSuccess = App.getInstance().mFacePassHandler.createLocalGroup(GROUP_NAME);
                                } catch (FacePassException e) {
                                    e.printStackTrace();
                                    LoadingDialog.hideLoading();
                                    showMsgDialog(false, "KS SDK初始化失败" + e.getMessage());
                                    return;
                                }
                                Logger.d("first 创建" + GROUP_NAME + isSuccess);
                            } else {
                                boolean isLocalGroupExist = false;
                                for (String group : localGroups) {
                                    if (GROUP_NAME.equals(group)) {
                                        isLocalGroupExist = true;
                                        Logger.d("已存在" + GROUP_NAME);
                                    }
                                }
                                if (!isLocalGroupExist) {
                                    boolean isSuccess = false;
                                    try {
                                        isSuccess = App.getInstance().mFacePassHandler.createLocalGroup(GROUP_NAME);
                                    } catch (FacePassException e) {
                                        e.printStackTrace();
                                        LoadingDialog.hideLoading();
                                        showMsgDialog(false, "KS SDK初始化失败" + e.getMessage());
                                        return;
                                    }
                                    Logger.d("second 创建" + GROUP_NAME + isSuccess);
                                }
                            }

                            LoadingDialog.hideLoading();
                            // 初始化成功
                            loopHandler.sendEmptyMessage(MSG_INIT_SUC);
                            // 刷新一下数据
                            EmployeeDBTool.getInstance().init();

                        } catch (FacePassException e) {
                            e.printStackTrace();
                            Log.d("WJY", "FacePassHandler is null");
                            LoadingDialog.hideLoading();
                            showMsgDialog(false, "KS SDK初始化失败" + e.getMessage());
                            return;
                        }
                        return;
                    }
                    try {
                        /* 如果SDK初始化未完成则需等待 */
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void showExitDialog(boolean showLeft, String content){
        String left = "";
        if(showLeft){
            left = "取消";
        }
        CommonDialog dialog = new CommonDialog(this, "提示", content, left,
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
        dialog.show();
    }

    private void showPermissionDialog(){
        CommonDialog dialog = new CommonDialog(this, "提示", "本程序需要存储和摄像头权限, 请到设置界面打开, 否则程序无法正常运行", "退出",
                "确定", new CommonDialog.BtnClickListener() {
            @Override
            public void onNegativeClick() {
                finish();
            }

            @Override
            public void onPositiveClick() {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                finish();
            }
        });
        dialog.show();
    }

    private void showMsgDialog(boolean showLeft, String content){
        String left = "";
        if(showLeft){
            left = "取消";
        }
        CommonDialog dialog = new CommonDialog(this, "提示", content, left,
                "确定", new CommonDialog.BtnClickListener() {
            @Override
            public void onNegativeClick() {

            }

            @Override
            public void onPositiveClick() {

            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        showExitDialog(true, "确定退出程序");
    }



    private class LoopHandler extends Handler {
        private WeakReference<Context> reference;

        public LoopHandler(WeakReference<Context> reference){
            this.reference = reference;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_INIT_SUC:
                    Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    }

}
