package megvii.testfacepass.db;

import megvii.testfacepass.App;
import megvii.testfacepass.utils.Logger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DBOpenHelper类
 * 现有的ORM存在M:N关系均存在问题,所以还是自己写吧
 * Created by codemaster on 2015/10/23.
 */
public class DBOpenHelper extends SQLiteOpenHelper {

    // 项目数据库名称和版本
    private static final String DATABASE_NAME = "x3d.db"; // 表示数据库的名称
    private static final int DATABASE_VERSION = 1;

    public static final String AUTOINCREMENTID = "aid";
    public static final String TAB_EMPLOYEE = "tab_employee";

    // employee
    public static final String EMPLOYEE_ID = "id";
    public static final String EMPLOYEE_NAME = "name";
    public static final String EMPLOYEE_FEATURE = "feature";
    public static final String EMPLOYEE_IMAGEPATH = "image_path";
    public static final String EMPLOYEE_IMAGEURL = "image_url";

    private DBOpenHelper(Context context, String name,
                         SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private static DBOpenHelper helper = new DBOpenHelper(App.getInstance(), DATABASE_NAME, null,
            DATABASE_VERSION);

    /**
     * 获取单例的openhelper
     *
     * @return
     */
    public static DBOpenHelper getHelper() {
        return helper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logger.d("onCreate" + db);
        // 抛弃自增键防止无穷增大，采用联合主键
        String sqlUser =
                "CREATE TABLE " + TAB_EMPLOYEE + " (" +  EMPLOYEE_ID + " TEXT, " + EMPLOYEE_NAME + " TEXT, "
                        + EMPLOYEE_FEATURE + " TEXT, " + EMPLOYEE_IMAGEPATH + " TEXT, "
                        + EMPLOYEE_IMAGEURL + " TEXT, primary key (" + EMPLOYEE_ID + "));";
        db.execSQL(sqlUser);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.d("onUpgrade" + oldVersion + ";" + newVersion);
    }
}
