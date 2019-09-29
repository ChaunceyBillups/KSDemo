package com.example.ksdemo.activity;


import java.util.ArrayList;
import java.util.List;

import com.example.ksdemo.R;
import com.example.ksdemo.adapter.EmployeeAdapter;
import com.example.ksdemo.db.EmployeeDBTool;
import com.example.ksdemo.model.Employee;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.Nullable;

public class EmployeeListActivity extends BaseActivity {

    private List<Employee> employeeList = new ArrayList<>();
    private ListView employeeListView;
    private EmployeeAdapter employeeAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_employeelist);
        initViews();
        initDatas();
    }

    private void initViews(){
        ((TextView) findViewById(R.id.topbar_title)).setText("人员列表");
        findViewById(R.id.topbar_left_back_panel).setOnClickListener(this);
        findViewById(R.id.topbar_left_back).setOnClickListener(this);
        employeeListView = findViewById(R.id.employee_list);
    }

    private void initDatas(){
        employeeList = EmployeeDBTool.getInstance().getRealTimeEmployees();
        employeeAdapter = new EmployeeAdapter(this, employeeList);
        employeeListView.setAdapter(employeeAdapter);
    }
}
