package com.example.ksdemo.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.example.ksdemo.App;
import com.example.ksdemo.R;
import com.example.ksdemo.utils.Logger;
import com.example.ksdemo.utils.ToastHelper;
import com.example.ksdemo.widget.LoadingDialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import megvii.facepass.FacePassException;
import megvii.facepass.types.FacePassCompareResult;

public class One2OneCompareActivity extends BaseActivity {

    private static final int REQUEST_IMAGE1 = 11;
    private static final int REQUEST_IMAGE2 = 12;
    private ArrayList<String> mSelectPath;

    private ImageView photoImg1;
    private ImageView photoImg2;
    private Button selectBtn1;
    private Button selectBtn2;
    private Button compareBtn;

    private Bitmap mBitmap1;
    private Bitmap mBitmap2;
    private String mPath1;
    private String mPath2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_one2onecompare);
        initViews();
    }

    private void initViews(){
        ((TextView) findViewById(R.id.topbar_title)).setText("图像1:1比对");
        findViewById(R.id.topbar_left_back_panel).setOnClickListener(this);
        findViewById(R.id.topbar_left_back).setOnClickListener(this);
        photoImg1 = findViewById(R.id.img_portrait1);
        photoImg2 = findViewById(R.id.img_portrait2);
        selectBtn1 = findViewById(R.id.btn_selectimg1);
        selectBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMultiImageSelector(REQUEST_IMAGE1);
            }
        });
        selectBtn2 = findViewById(R.id.btn_selectimg2);
        selectBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMultiImageSelector(REQUEST_IMAGE2);
            }
        });
        compareBtn = findViewById(R.id.btn_one2onecompare);
        compareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBitmap1 == null){
                    Toast.makeText(One2OneCompareActivity.this, "请选择第一张照片", Toast.LENGTH_LONG).show();
                    return;
                } else if(mBitmap2 == null){
                    Toast.makeText(One2OneCompareActivity.this, "请选择第一张照片", Toast.LENGTH_LONG).show();
                    return;
                }

                LoadingDialog.showLoading(One2OneCompareActivity.this, "请稍候...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            long start = System.currentTimeMillis();
                            FacePassCompareResult compareResult =
                                    App.getInstance().mFacePassHandler.compare(mBitmap1, mBitmap2, false);
                            long total = System.currentTimeMillis() - start;
                            LoadingDialog.hideLoading();
                            if (compareResult != null) {
                                // 0:成功 1:没有检测到人脸 2:检测到人脸,但是没有通过质量判断
                                if (compareResult.result == 0) {
                                    ToastHelper.showMessageInHandler(One2OneCompareActivity.this,
                                            "比对结束，相似分数为:" + compareResult.score + "耗时：" + total + "ms");
                                } else if (compareResult.result == 1) {
                                    ToastHelper.showMessageInHandler(One2OneCompareActivity.this, "照片没有检测到人脸，请重新选择");
                                }
                            } else {
                                ToastHelper.showMessageInHandler(One2OneCompareActivity.this, "比对失败，请查看日志");
                            }
                        } catch (FacePassException e) {
                            e.printStackTrace();
                            LoadingDialog.hideLoading();
                            ToastHelper.showMessageInHandler(One2OneCompareActivity.this, "比对异常，请查看日志");
                        }
                    }
                }).start();
            }
        });
    }

    private void openMultiImageSelector(int request) {
        Intent intent = new Intent(this, MultiImageSelectorActivity.class);
        // 是否显示拍摄图片
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        // 最大可选择图片数量
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 1);
        // 选择模式
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);
        if (mSelectPath != null && mSelectPath.size() > 0) {
            intent.putExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mSelectPath);
        }
        this.startActivityForResult(intent, request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE1) {
            if (resultCode == Activity.RESULT_OK) {
                mSelectPath = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                StringBuilder sb = new StringBuilder();
                for (String p : mSelectPath) {
                    sb.append(p);
                    //sb.append("\n"); 干你大爷，加个\n坑死劳资了，一直报找不到file 草草草
                }
                mPath1 = sb.toString();
                Logger.d("image", mPath1);

                File file = new File(mPath1);
                if (!file.exists()) {
                    Toast.makeText(this, "图片已被损坏，请重新选择", Toast.LENGTH_LONG).show();
                    return;
                }
                if (file.length() / 1024 > 1024) {
                    Toast.makeText(this, "图片大于1MB，请重新选择", Toast.LENGTH_LONG).show();
                    return;
                }

                FileInputStream fs = null;
                try {
                    fs = new FileInputStream(mPath1);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                mBitmap1  = BitmapFactory.decodeStream(fs);
                photoImg1.setImageBitmap(mBitmap1);
            }
        } else if (requestCode == REQUEST_IMAGE2) {
            if (resultCode == Activity.RESULT_OK) {
                mSelectPath = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                StringBuilder sb = new StringBuilder();
                for (String p : mSelectPath) {
                    sb.append(p);
                    //sb.append("\n"); 干你大爷，加个\n坑死劳资了，一直报找不到file 草草草
                }
                mPath2 = sb.toString();
                Logger.d("image", mPath2);

                File file = new File(mPath2);
                if (!file.exists()) {
                    Toast.makeText(this, "图片已被损坏，请重新选择", Toast.LENGTH_LONG).show();
                    return;
                }
                if (file.length() / 1024 > 1024) {
                    Toast.makeText(this, "图片大于1MB，请重新选择", Toast.LENGTH_LONG).show();
                    return;
                }

                FileInputStream fs = null;
                try {
                    fs = new FileInputStream(mPath2);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                mBitmap2  = BitmapFactory.decodeStream(fs);
                photoImg2.setImageBitmap(mBitmap2);
            }
        }
    }
}
