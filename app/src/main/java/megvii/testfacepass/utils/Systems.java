package megvii.testfacepass.utils;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.util.Log.w;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import megvii.testfacepass.App;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

/**
 * 系统工具
 * Created by Tony on 2017/8/4.
 */
public class Systems {

    private Systems() {
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    /**
     * dp to px
     *
     * @param dp
     * @param context
     * @return
     */
    public static int dpToPx(Context context, float dp) {
        return (int) applyDimension(context, TypedValue.COMPLEX_UNIT_DIP, dp);
    }

    /**
     * 单位转换
     *
     * @param context
     * @param unit    TypedValue.COMPLEX_UNIT_DIP
     * @param value   px
     * @return
     */
    public static float applyDimension(Context context, int unit, float value) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(unit, value, displayMetrics);
    }

    /**
     * 打开软键盘
     *
     * @param window
     */
    public static void showKeyboard(Window window, View view) {
        if (window.getCurrentFocus() != null) {
            InputMethodManager inputManager = (InputMethodManager) window.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (view != null) {
                inputManager.showSoftInput(view, 0);
            }
            inputManager.showSoftInputFromInputMethod(window.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static void showKeyboard(final View view, int delay) {
        if (view != null) {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) view.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            }, delay);
        }
    }

    /**
     * 关闭软键盘
     *
     * @param context
     */
    public static void hideKeyboard(Context context) {
        try {
            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = ((Activity) context).getCurrentFocus();
            if (view == null) {
                return;
            }
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            Log.e("Sigh can't event hide ", e.getMessage());
        }
    }

    public static void hideKeyboard(View view) {
        try {
            InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            Log.e("Sigh can't event hide ", e.getMessage());
        }
    }

    /**
     * 获取App安装包信息
     *
     * @return
     */
    public static PackageInfo getPackageInfo(Context context) {
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("程序包名无法找到", e.getMessage());
        }
        if (info == null)
            info = new PackageInfo();
        return info;
    }

    /**
     * 获取当前程序版本名称
     */
    public static String getVersionName(Context context) {
        return getVersionName(context, context.getPackageName());
    }

    /**
     * 获取程序版本名称
     */
    public static String getVersionName(Context context, String packageName) {
        String versionName;
        try {
            // Get the package info
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = pi.versionName;
            if (TextUtils.isEmpty(versionName)) {
                return "";
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("获取程序版本名称", e);
            return "";
        }
        return versionName;
    }

    /**
     * 获取当前代码版本号
     */
    public static int getVersionCode(Context context) {
        int localVersion = 0;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            localVersion = pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            w("获取当前代码版本号", e);
        }
        return localVersion;
    }

    /**
     * 获取当前窗口的旋转角度
     *
     * @param activity
     * @return
     */
    public static int getDisplayRotation(Activity activity) {
        switch (activity.getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                return 0;
        }
    }

    /**
     * 当前是否是横屏
     *
     * @param context
     * @return
     */
    public static final boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 当前是否是竖屏
     *
     * @param context
     * @return
     */
    public static final boolean isPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * 是否平板设备
     *
     * @param context
     * @return
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static int getNavigationBarSize(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    /**
     * 获取总的运行内存
     */
    public static String getTotalMemory(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return (mi.totalMem / 1048576L) + "M";
    }

    /**
     * 获取手机的MAC地址
     */
    public static String getMac() {
        String macAddress = "";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iF = interfaces.nextElement();

                byte[] addr = iF.getHardwareAddress();
                if (addr == null || addr.length == 0) {
                    continue;
                }

                StringBuilder buf = new StringBuilder();
                for (byte b : addr) {
                    buf.append(String.format("%02X:", b));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                String mac = buf.toString();
                if (TextUtils.equals(iF.getName(), "wlan0")) {
                    return mac;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            return macAddress;
        }
        return macAddress;
    }

    public static String getIpAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            // 3/4g网络
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                //  wifi网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());
                return ipAddress;
            } else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
                // 有限网络
                return getLocalIp();
            }
        }
        return null;
    }

    private static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }


    // 获取有限网IP
    private static String getLocalIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {

        }
        return "0.0.0.0";
    }

    /**
     * 获取设备型号
     *
     * @return
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * 获取剩余可用内存
     *
     * @return
     */
    public static float getAvailableSize() {
        String storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        StatFs fs = new StatFs(storage);
        long availableBlocks = fs.getAvailableBlocksLong();
        long blockSize = fs.getBlockSizeLong();
        return (float) (availableBlocks * blockSize / (1024 * 1024 * 1024.0));
    }

    /**
     * 递归删除指定目录下的空文件夹
     *
     * @param dir
     */
    public static void clearDirs(File dir) {
        File[] dirs = dir.listFiles();
        for (int i = 0; i < dirs.length; i++) {
            if (dirs[i].isDirectory()) {
                clearDirs(dirs[i]);
            }
        }
        if (dir.isDirectory()) {
            dir.delete();
        }
    }

    /**
     * 彻底删除某个文件夹及其下属子文件夹和文件
     * @param path
     */
    public static void delDir(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            File[] tmp = dir.listFiles();
            for (int i = 0; i < tmp.length; i++) {
                if (tmp[i].isDirectory()) {
                    delDir(path + File.separator + tmp[i].getName());
                } else {
                    tmp[i].delete();
                }
            }
            dir.delete();
        }
    }

    public static void delFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public static byte[] NV21_rotate_to_270(byte[] nv21_data, int width, int height) {
        int y_size = width * height;
        int buffser_size = y_size * 3 / 2;
        byte[] nv21_rotated = new byte[buffser_size];
        int i = 0;

        // Rotate the Y luma
        for (int x = width - 1; x >= 0; x--) {
            int offset = 0;
            for (int y = 0; y < height; y++) {
                nv21_rotated[i] = nv21_data[offset + x];
                i++;
                offset += width;
            }
        }

        // Rotate the U and V color components
        i = y_size;
        for (int x = width - 1; x > 0; x = x - 2) {
            int offset = y_size;
            for (int y = 0; y < height / 2; y++) {
                nv21_rotated[i] = nv21_data[offset + (x - 1)];
                i++;
                nv21_rotated[i] = nv21_data[offset + x];
                i++;
                offset += width;
            }
        }
        return nv21_rotated;
    }


    public static byte[] NV21_rotate_to_180(byte[] nv21_data, int width, int height) {
        long startTime = System.currentTimeMillis();
        int y_size = width * height;
        int buffser_size = y_size * 3 / 2;
        byte[] nv21_rotated = new byte[buffser_size];
        int i = 0;
        int count = 0;

        for (i = y_size - 1; i >= 0; i--) {
            nv21_rotated[count] = nv21_data[i];
            count++;
        }

        for (i = buffser_size - 1; i >= y_size; i -= 2) {
            nv21_rotated[count++] = nv21_data[i - 1];
            nv21_rotated[count++] = nv21_data[i];
        }
        long endTime = System.currentTimeMillis();
        long total = endTime - startTime;
        // Logger.d("cameraview", "旋转耗时：" + total);
        return nv21_rotated;
    }

    public static byte[] NV21_rotate_to_90(byte[] nv21_data, int width, int height) {
        int y_size = width * height;
        int buffser_size = y_size * 3 / 2;
        byte[] nv21_rotated = new byte[buffser_size];
        // Rotate the Y luma

        int i = 0;
        int startPos = (height - 1) * width;
        for (int x = 0; x < width; x++) {
            int offset = startPos;
            for (int y = height - 1; y >= 0; y--) {
                nv21_rotated[i] = nv21_data[offset + x];
                i++;
                offset -= width;
            }
        }

        // Rotate the U and V color components
        i = buffser_size - 1;
        for (int x = width - 1; x > 0; x = x - 2) {
            int offset = y_size;
            for (int y = 0; y < height / 2; y++) {
                nv21_rotated[i] = nv21_data[offset + x];
                i--;
                nv21_rotated[i] = nv21_data[offset + (x - 1)];
                i--;
                offset += width;
            }
        }
        return nv21_rotated;
    }

    /*
     * bitmap转base64
     * */
    public static String bitmapToBase64(Bitmap bitmap, int quality) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    public static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getChanne() {
        AssetManager am = App.getInstance().getAssets();

        BufferedInputStream is = null;
        BufferedReader br = null;
        String content = "";
        try {

            is = new BufferedInputStream(am.open("channel"));

            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String sCurrentLine = null;
            while ((sCurrentLine = br.readLine()) != null) {
                content += sCurrentLine;
            }

            return content;
        } catch (IOException ioe) {
            return "default";
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {

            }
        }
    }

    /**
     * 获取手机序列号
     *
     * @return 手机序列号
     */
    @SuppressLint({"NewApi", "MissingPermission"})
    public static String getSN() {
        String serial = "";
        try {
            // 9.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                serial = Build.getSerial();
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                // 8.0+
                serial = Build.SERIAL;
            } else {// 8.0-
                Class<?> c = Class.forName("android.os.SystemProperties");
                Method get = c.getMethod("get", String.class);
                serial = (String) get.invoke(c, "ro.serialno");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("e", "读取设备序列号异常：" + e.toString());
        }
        return serial;
    }
}
