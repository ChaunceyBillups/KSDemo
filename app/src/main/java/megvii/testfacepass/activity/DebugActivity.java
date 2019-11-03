package megvii.testfacepass.activity;

import megvii.testfacepass1.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 调试用的Activity
 */
public class DebugActivity extends AppCompatActivity implements View.OnClickListener{


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_debug);
        initViews();
    }

    private void initViews(){
        findViewById(R.id.employee_add_panel).setOnClickListener(this);
        findViewById(R.id.employee_status_panel).setOnClickListener(this);
        findViewById(R.id.employee_oneonecompare).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.employee_add_panel:
                Intent intent = new Intent(this, AddEmployeeActivity.class);
                startActivity(intent);
                break;
            case R.id.employee_status_panel:
                Intent intent1 = new Intent(this, EmployeeListActivity.class);
                startActivity(intent1);
                break;
            case R.id.employee_oneonecompare:
                Intent intent2 = new Intent(this, megvii.testfacepass.activity.One2OneCompareActivity.class);
                startActivity(intent2);
                break;
        }
    }
}
