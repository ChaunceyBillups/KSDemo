package megvii.testfacepass.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import megvii.testfacepass1.R;
import com.megvii.authapilib.AuthApi;
import com.megvii.authapilib.ReturnInfo;

public class AuthActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MEGVII-LICENSE";
    private TextView mAuthStatus;
    /* 程序所需权限 ：相机 文件存储 网络访问 */
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String PERMISSION_READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String PERMISSION_INTERNET = Manifest.permission.INTERNET;
    private static final String PERMISSION_ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE;
    private String[] Permission = new String[]{ PERMISSION_WRITE_STORAGE, PERMISSION_READ_STORAGE, PERMISSION_INTERNET, PERMISSION_ACCESS_NETWORK_STATE};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        mAuthStatus = (TextView)findViewById(R.id.textView_auth_status);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_fingerprint:
                if (!hasPermission()) {
                    requestPermission();
                } else {
                    generate_fingerprint();
                }

                break;
            case R.id.button_activate:
                activate_device();
                break;
            case R.id.button_ok:
                enter_mainactivity();
                break;
        }
    }


    private  void generate_fingerprint() {
        Log.d(TAG, "generate_fingerprint enter ");
        AuthApi obj = new AuthApi();
        ReturnInfo text = new ReturnInfo();
        obj.GenC2vFile(text);

        mAuthStatus.setTextColor(Color.BLUE);
        mAuthStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mAuthStatus.setBackgroundColor(Color.YELLOW);
        mAuthStatus.setText(text.retInfo);

        Log.d(TAG, text.retInfo);
        Log.d(TAG, "generate_fingerprint exit ");
    }

    private  void activate_device() {
        Log.d(TAG, "activate_device enter ");
        String path = "update.v2c";
        AuthApi obj = new AuthApi();
        ReturnInfo text = new ReturnInfo();
        obj.AuthDevice(path, text);

        mAuthStatus.setTextColor(Color.BLUE);
        mAuthStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mAuthStatus.setBackgroundColor(Color.YELLOW);
        mAuthStatus.setText(text.retInfo);

        Log.d(TAG, text.retInfo);
        Log.d(TAG, "activate_device exit ");
    }

    private  void enter_mainactivity() {
        String strAuth = mAuthStatus.getText().toString();
        if (strAuth.equals("Apply update: OK")
                || strAuth.equals("Apply update: Update already added")) {
            Intent intent = new Intent(AuthActivity.this, megvii.testfacepass.activity.WelcomeActivity.class);
            startActivity(intent);
            AuthActivity.this.finish();
        }
    }
    /* 请求程序所需权限 */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(Permission, PERMISSIONS_REQUEST);
        }
    }
    /* 判断程序是否有所需权限 android22以上需要自申请权限 */
    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return
                    checkSelfPermission(PERMISSION_READ_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_WRITE_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_INTERNET) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }
}
