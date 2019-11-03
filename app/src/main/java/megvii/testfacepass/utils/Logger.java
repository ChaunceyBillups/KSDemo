package megvii.testfacepass.utils;

import android.util.Log;

public class Logger {

    public static final boolean DEBUG = true;
    private static final String TAG = "faceunlock2";


    public static void d(String msg){
        Log.d(TAG, msg);
    }

    public static void d(String tag, String msg){
        Log.d(tag, msg);
    }

    public static void e(String msg){
        Log.e(TAG, msg);
    }
}
