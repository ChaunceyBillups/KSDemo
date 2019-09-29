package com.example.ksdemo;

import java.util.ArrayList;
import java.util.List;

import com.example.ksdemo.utils.UncaughtExceptionHandler;

import android.app.Activity;
import android.app.Application;
import megvii.facepass.FacePassHandler;

public class App extends Application {

    private static App instance;
    public FacePassHandler mFacePassHandler = null;
    public int cameraPreviewWidth;
    public int cameraPreviewHeight;
    public volatile boolean isPauseRecognize;
    private List<Activity> activityList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        UncaughtExceptionHandler.getInstance().init();
    }

    public static App getInstance() {
        return instance;
    }

    public void addActivity(Activity activity){
        activityList.add(activity);
    }

    public void removeActivity(Activity activity){
        activityList.remove(activity);
    }

    public void clearAllActivites(){
        for(int i = 0; i < activityList.size(); i++){
            Activity activity = activityList.get(i);
            if(activity != null){
                activity.finish();
            }
        }
    }
}
