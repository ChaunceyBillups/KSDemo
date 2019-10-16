package com.example.ksdemo.activity;

import com.example.ksdemo.R;
import com.example.ksdemo.cameratest.TestCameraActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;

/**
 * 调试用的Activity
 */
public class DebugActivity extends BaseActivity implements View.OnClickListener{


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_debug);
        initViews();
    }

    private void initViews(){
        ((TextView) findViewById(R.id.topbar_title)).setText("调试模式");
        findViewById(R.id.topbar_left_back_panel).setOnClickListener(this);
        findViewById(R.id.topbar_left_back).setOnClickListener(this);
        findViewById(R.id.employee_add_panel).setOnClickListener(this);
        findViewById(R.id.employee_status_panel).setOnClickListener(this);
        findViewById(R.id.employee_oneonecompare).setOnClickListener(this);
        findViewById(R.id.test_camera).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.topbar_left_back_panel:
            case R.id.topbar_left_back:
                finish();
                break;
            case R.id.employee_add_panel:
                Intent intent = new Intent(this, AddEmployeeActivity.class);
                startActivity(intent);
                break;
            case R.id.employee_status_panel:
                Intent intent1 = new Intent(this, EmployeeListActivity.class);
                startActivity(intent1);
                break;
            case R.id.employee_oneonecompare:
                Intent intent2 = new Intent(this, One2OneCompareActivity.class);
                startActivity(intent2);
                break;
            case R.id.test_camera:
                Intent intent3 = new Intent(this, TestCameraActivity.class);
                startActivity(intent3);
                break;
        }
    }
}
