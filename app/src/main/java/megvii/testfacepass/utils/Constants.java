package megvii.testfacepass.utils;

import java.io.File;

import android.os.Environment;

public class Constants {

    // public static final String DEVICENUM = "20190625001";
    public static final String GROUP_NAME = "ksdemo_default";
    public static String DEVICENUM = "20190716001";
    public static final String SDPATH = Environment.getExternalStorageDirectory().getPath() + File
            .separator + "ksdemo";



    public static final String FEATURE_TYPE_KS = "ks";
    public static final String FEATURE_TYPE_ST = "st";


    public static String CUR_FEATURE_TYPE = "no";

    public static String SERVER_IP = "http://47.92.210.238:8090";

    public static long lastTs = 0;

    public static String CompanyName = "";

    // 同步人员时单次更新的人数
    public static final int PAGE_COUNT = 20;
    // 不进行任何操作
    public static final int OPERATION_0 = 0;
    // 更新人员信息
    public static final int OPERATION_1 = 1;
    // 更新设备参数
    public static final int OPERATION_2 = 2;
    // 重启设备
    public static final int OPERATION_3 = 3;
    // 重置数据
    public static final int OPERATION_4 = 4;



    public static final int DEVICETYPE_PHONE = 1;
    public static final int DEVICETYPE_QZ = 2;



}
