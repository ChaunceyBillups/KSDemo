package megvii.testfacepass.activity;


import java.util.ArrayList;
import java.util.List;

import megvii.testfacepass1.R;
import megvii.testfacepass.adapter.EmployeeAdapter;
import megvii.testfacepass.db.EmployeeDBTool;
import megvii.testfacepass.model.Employee;
import android.os.Bundle;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class EmployeeListActivity extends AppCompatActivity {

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
        employeeListView = findViewById(R.id.employee_list);
    }

    private void initDatas(){
        employeeList = EmployeeDBTool.getInstance().getRealTimeEmployees();
        employeeAdapter = new EmployeeAdapter(this, employeeList);
        employeeListView.setAdapter(employeeAdapter);
    }
}
