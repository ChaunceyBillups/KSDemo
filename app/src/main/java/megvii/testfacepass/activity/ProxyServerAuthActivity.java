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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.megvii.authapilib.AuthApi;
import com.megvii.authapilib.ReturnInfo;
import com.megvii.netapi.MyHttpHandler;
import com.megvii.netapi.NetApi;

import megvii.testfacepass1.R;

public class ProxyServerAuthActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MEGVII-LICENSE";

    /* 程序所需权限 ：相机 文件存储 网络访问 */
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String PERMISSION_READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String PERMISSION_INTERNET = Manifest.permission.INTERNET;
    private static final String PERMISSION_ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE;
    private String[] Permission = new String[]{PERMISSION_CAMERA, PERMISSION_WRITE_STORAGE, PERMISSION_READ_STORAGE, PERMISSION_INTERNET, PERMISSION_ACCESS_NETWORK_STATE};

    private TextView mAuthStatus;

    private authThread mAuthThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        mAuthStatus = (TextView)findViewById(R.id.textView_auth_status);

        /* 申请程序所需权限 */
        if (!hasPermission()) {
            requestPermission();
        }
        mAuthStatus.setTextColor(Color.BLUE);
        mAuthStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mAuthStatus.setBackgroundColor(Color.YELLOW);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_fingerprint:
                generateFingerprint();
                break;
            case R.id.button_activate:
                activateDevice();
                break;
            case R.id.button_ok:
                enterMainactivity();
                break;
        }
    }
    private  void generateFingerprint() {
        Log.d(TAG, "generate_fingerprint enter ");
        AuthApi obj = new AuthApi();
        ReturnInfo text = new ReturnInfo();
        obj.GenC2vFile(text);

        mAuthStatus.setText(text.retInfo);

        Log.d(TAG, text.retInfo);
        Log.d(TAG, "generate_fingerprint exit ");
    }


    private boolean getActivateFile() {
        NetApi api = new NetApi();
        return api.GetAuthFileByKey("CBG_Android_Face_Reco---30-Trial-one-stage.cert");
    }
//无网批量授权
    private void getActivateFileFromHost() {
        NetApi api = new NetApi();
        api.GetAuthFileByProxy("10.156.6.226", "6666", "CBG_Android_Face_Reco---30-Trial-one-stage.cert", "");
    }

    private void showAuthResult(String res) {
        mAuthStatus.setText(res);
    }

    private class authThread extends Thread {

        @Override
        public void run() {
            try {
//                boolean res = getActivateFile();  //端上联网获取授权
//                if (!res) {
//                    Log.d(TAG, "after getActivateFile result: " + res);
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            showAuthResult("getActivateFile fail ... ...");
//                        }
//                    });
//
//                    return;
//                }

                getActivateFileFromHost();   //通过代理服务器联网获取授权

                Thread.sleep(1000);
                while (true) {
                    String netRes = MyHttpHandler.GetNetResult();
                    Log.d(TAG, "GetNetResult: " + netRes);
                    if (netRes.equals("org")) {
                        Thread.sleep(1000);
                        String errInfo = MyHttpHandler.GetErrorInfo();
                        Log.d(TAG, errInfo);
                        continue;
                    } else if (netRes.equals("ok")) {
                        break;
                    } else {
                        String errInfo = MyHttpHandler.GetErrorInfo();
                        Log.d(TAG, errInfo);
                        return;
                    }
                }

                Log.d(TAG, "before ActivateDevice");
                String path = "update.v2c";
                AuthApi obj = new AuthApi();
                final ReturnInfo retunInfo = new ReturnInfo();
                obj.AuthDevice(path, retunInfo);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAuthResult(retunInfo.retInfo);
                    }
                });

                Log.d(TAG, retunInfo.retInfo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private  void activateDevice() {
        Log.d(TAG, "activate_device enter ");

        mAuthThread = new authThread();
        mAuthThread.start();

        Log.d(TAG, "activate_device exit ");
    }

    private  void enterMainactivity() {
        String strAuth = mAuthStatus.getText().toString();
        if (strAuth.equals("Apply update: OK")
                || strAuth.equals("Apply update: Update already added")) {
            Log.d(TAG, "before enter MainActivity ");
            Intent intent = new Intent(ProxyServerAuthActivity.this, WelcomeActivity.class);
            startActivity(intent);
            ProxyServerAuthActivity.this.finish();
            /* 用户可以记录设备激活状态，在下次启动app时，直接进入主界面 */
            /* 用户可以记录设备激活状态，在下次启动app时，直接进入主界面 */
            /* 用户可以记录设备激活状态，在下次启动app时，直接进入主界面 */
        }
    }

    /* 判断程序是否有所需权限 android22以上需要自申请权限 */
    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_READ_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_WRITE_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_INTERNET) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    /* 请求程序所需权限 */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(Permission, PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED)
                    granted = false;
            }
            if (!granted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    if (!shouldShowRequestPermissionRationale(PERMISSION_CAMERA)
                            || !shouldShowRequestPermissionRationale(PERMISSION_READ_STORAGE)
                            || !shouldShowRequestPermissionRationale(PERMISSION_WRITE_STORAGE)
                            || !shouldShowRequestPermissionRationale(PERMISSION_INTERNET)
                            || !shouldShowRequestPermissionRationale(PERMISSION_ACCESS_NETWORK_STATE)) {
                        Toast.makeText(getApplicationContext(), "需要开启摄像头网络文件存储权限", Toast.LENGTH_SHORT).show();
                    }
            }
        }
    }

}
