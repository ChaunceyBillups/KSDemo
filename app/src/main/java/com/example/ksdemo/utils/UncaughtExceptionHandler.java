package com.example.ksdemo.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.example.ksdemo.App;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

/**
 * crash日志，这个类不用开线程了
 * Created by codemaster on 18/3/23.
 */
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static volatile boolean mCrashing = false;
    private Context mContext;
    // 用来存储设备信息和异常信息
    private HashMap<String, String> infos = new HashMap<>();
    private File fileDirectory;
    private BufferedReader bufferedReader;
    private FileReader fileReader;
    private File[] files;
    private String fileDir;

    public UncaughtExceptionHandler() {
    }

    private static class Holer {
        private static UncaughtExceptionHandler instance = new UncaughtExceptionHandler();
    }

    public static UncaughtExceptionHandler getInstance() {
        return Holer.instance;
    }

    public void init() {
        mContext = App.getInstance();
        Thread.setDefaultUncaughtExceptionHandler(this);
        fileDir = Constants.SDPATH + File.separator + "log";
        fileDirectory = new File(fileDir);
        if (!fileDirectory.exists()) {
            fileDirectory.mkdirs();
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (mCrashing) {
            return;
        }
        mCrashing = true;
        if (!handleException(throwable)) {
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, throwable);
        }
        mCrashing = false;
        try {
            // 暂停3秒
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }
        App.getInstance().clearAllActivites();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    private boolean handleException(Throwable throwable) {
        // 使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "程序出现异常，即将退出～ 日志保存在SD卡 ksdemo/log", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();
        if (throwable == null) {
            return false;
        }
        collectDeviceInfo();
        saveCatchInfo(throwable);
        return true;
    }

    private void saveCatchInfo(final Throwable ex) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        Systems.close(printWriter);
        String result = writer.toString();
        sb.append(result);
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + ".log";

            File file = new File(fileDirectory, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(sb.toString().getBytes());
            Systems.close(fos);
            // 发送给服务端的时机
        } catch (Exception e) {
            Logger.d("an error occured while writing file..." + e.getMessage());
        }
    }

    private void collectDeviceInfo() {
        Logger.d("CollectDeviceInfo");
        // 手机型号信息
        infos.clear();
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                Logger.d("an error occured when collect crash info" + e.toString());
            }
        }

        // 安装包的信息
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logger.d("an error occured when collect package info" + e.toString());
        }
    }

    // 上传服务器
    public void upLoadAndDelete() {
        if (fileDirectory == null || !fileDirectory.exists()) {
            fileDirectory = new File(fileDir);
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs();
            }
        }
        // 路径所有的文件
        files = fileDirectory.listFiles();
        if (files == null || files.length <= 0) {
            return;
        }

        String content = "";
        try {
            fileReader = new FileReader(files[0]);
            bufferedReader = new BufferedReader(fileReader);
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                content = content + str;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("channel", Systems.getVersionName(mContext));
//        hashMap.put("mb", SysOSAPI.getInstance().getPhoneType());
//        hashMap.put("os", SysOSAPI.getInstance().getPhoneOS());
//        hashMap.put("cuid", SysOSAPI.getInstance().getCUID());
//        hashMap.put("sdkVer", SysOSAPI.getInstance().getVersionName());
//        hashMap.put("ndid", SysOSAPI.getInstance().getNDID());
//        hashMap.put("mpk", SysOSAPI.getInstance().getMPK());
//        hashMap.put("prod", SysOSAPI.getInstance().getProd());
//        hashMap.put("data", "[" + content + "]");
//        HttpClient.getInstane().asyncPost(MECP_URL, "reportCrashLog", hashMap, new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (files != null && files.length > 0) {
//                    files[0].delete();
//                }
//                upLoadAndDelete();
//            }
//        });
    }
}
