package com.example.ksdemo.utils;

import java.io.File;

import android.os.Environment;

public class Constants {

    public static final String GROUP_NAME = "ksdemo_default";
    public static final String SDPATH = Environment.getExternalStorageDirectory().getPath() + File
            .separator + "ksdemo";

    // 表示普通的手机
    public static final int DEVICETYPE_PHONE = 1;
    // 表示QZ提供的设备
    public static final int DEVICETYPE_QZ = 2;



}
