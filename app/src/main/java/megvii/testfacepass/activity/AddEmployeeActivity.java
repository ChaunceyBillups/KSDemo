package megvii.testfacepass.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.UUID;

import megvii.testfacepass.App;
import megvii.testfacepass.db.EmployeeDBTool;
import megvii.testfacepass.model.Employee;
import megvii.testfacepass.utils.Logger;
import megvii.testfacepass.utils.ToastHelper;
import megvii.testfacepass.widget.LoadingDialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import megvii.facepass.FacePassException;
import megvii.facepass.types.FacePassAddFaceResult;
import megvii.testfacepass.App;
import megvii.testfacepass.widget.LoadingDialog;
import megvii.testfacepass1.R;

import static megvii.testfacepass.utils.Constants.GROUP_NAME;

public class AddEmployeeActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 10;
    private ArrayList<String> mSelectPath;

    private ImageView photoImg;
    private Button selectBtn;
    private EditText nameEditText;
    private Button addBtn;

    private Bitmap mBitmap;
    private String mPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_addemployee);
        initViews();
    }

    private void initViews(){
        photoImg = findViewById(R.id.img_portrait);
        selectBtn = findViewById(R.id.btn_selectimg);
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMultiImageSelector();
            }
        });
        nameEditText = findViewById(R.id.name_edit);
        addBtn = findViewById(R.id.btn_addemployee);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBitmap == null){
                    Toast.makeText(AddEmployeeActivity.this, "请选择照片", Toast.LENGTH_LONG).show();
                    return;
                }
                final String name = nameEditText.getText().toString().trim();
                if(TextUtils.isEmpty(name)){
                    Toast.makeText(AddEmployeeActivity.this, "请输入姓名", Toast.LENGTH_LONG).show();
                    return;
                }
                LoadingDialog.showLoading(AddEmployeeActivity.this, "请稍候...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            FacePassAddFaceResult faceResult =
                                    App.getInstance().mFacePassHandler.addFace(mBitmap);
                            if (faceResult != null) {
                                // 0:成功 1:没有检测到人脸 2:检测到人脸,但是没有通过质量判断
                                if (faceResult.result == 0) {
                                    //                            employee.setFeature(new String(faceResult.faceToken));
                                    //                            employee.setFeatureType(Constants.FEATURE_TYPE_KS);
                                    //                            mFacePassHandler.bindGroup(GROUP_NAME, faceResult.faceToken);
                                    Employee employee = new Employee();
                                    String uuid = UUID.randomUUID().toString();
                                    employee.setId(uuid);
                                    employee.setName(name);
                                    employee.setFeature(new String(faceResult.faceToken));
                                    employee.setImagePath(mPath);
                                    boolean flage = App.getInstance().mFacePassHandler.bindGroup(GROUP_NAME,
                                            faceResult.faceToken);
                                    LoadingDialog.hideLoading();
                                    if(flage){
                                        EmployeeDBTool.getInstance().insertEmployee(employee);
                                        ToastHelper.showMessageInHandler(AddEmployeeActivity.this, "入库成功");
                                    } else {
                                        ToastHelper.showMessageInHandler(AddEmployeeActivity.this, "SDK入库绑定失败");
                                    }
                                } else {
                                    LoadingDialog.hideLoading();
                                    ToastHelper.showMessageInHandler(AddEmployeeActivity.this, "照片没有检测到人脸，请重新选择");
                                }
                            }
                        } catch (FacePassException e) {
                            e.printStackTrace();
                            LoadingDialog.hideLoading();
                            ToastHelper.showMessageInHandler(AddEmployeeActivity.this, "入库异常，请查看日志");
                        }
                    }
                }).start();
            }
        });
    }

    private void openMultiImageSelector() {
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
        this.startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                mSelectPath = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                StringBuilder sb = new StringBuilder();
                for (String p : mSelectPath) {
                    sb.append(p);
                    //sb.append("\n"); 干你大爷，加个\n坑死劳资了，一直报找不到file 草草草
                }
                mPath = sb.toString();
                Logger.d("image", mPath);

                File file = new File(mPath);
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
                    fs = new FileInputStream(mPath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                mBitmap  = BitmapFactory.decodeStream(fs);
                photoImg.setImageBitmap(mBitmap);
            }
        }
    }
}
