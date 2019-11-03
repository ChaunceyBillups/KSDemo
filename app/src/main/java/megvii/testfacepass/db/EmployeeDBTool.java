package megvii.testfacepass.db;

import java.util.ArrayList;
import java.util.List;

import megvii.testfacepass.model.Employee;
import megvii.testfacepass.utils.Logger;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

/**
 * User的数据库操作类
 * Created by codemaster on 2016/8/2.
 */
public class EmployeeDBTool {

    private static SQLiteDatabase db = null;
    private List<Employee> employeeList = new ArrayList<>();
    private static class Holder{
        public static EmployeeDBTool instance = new EmployeeDBTool();
    }
    public static EmployeeDBTool getInstance() {
        return Holder.instance;
    }

    private EmployeeDBTool() {

    }

    public void init(){
        employeeList = getAllEmployees();
    }

    public void insertEmpoyees(List<Employee> employeeList){
        if (employeeList == null || employeeList.size() == 0) {
            return;
        }
        db = DBOpenHelper.getHelper().getWritableDatabase();
        try {
            for (int i = 0; i < employeeList.size(); i++) {
                insertEmployee(db, employeeList.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        refresh();
    }

    private long insertEmployee(SQLiteDatabase db, Employee e) {
//        if(TextUtils.isEmpty(e.getFeature())){
//            return -1;
//        }
        Cursor c = null;
        long rowid = -1;
        try {
            ContentValues cv = getEmployeeContentValues(e);
            String sql = "select * from tab_employee where id = ? and imageId = ?";
            c = db.rawQuery(sql, new String[]{e.getId()});
            if (c.moveToFirst()) {
                Logger.d("employee已存在，更新:" + e.getName());
                db.update(DBOpenHelper.TAB_EMPLOYEE, cv, "id = ? and imageId = ?", new String[]{e.getId()});
                return 0;
            }
            Logger.d("employee不存在，插入:" + e.getName());
            rowid = db.insert(DBOpenHelper.TAB_EMPLOYEE, null, cv);
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return rowid;
    }

    public long insertEmployee(Employee employee){
        db = DBOpenHelper.getHelper().getWritableDatabase();
        long rowid = -1;
        try {
            ContentValues cv = getEmployeeContentValues(employee);
            Logger.d("db插入employee:" + employee.getName());
            rowid = db.insert(DBOpenHelper.TAB_EMPLOYEE, null, cv);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        refresh();
        return rowid;
    }

    public long modifyEmployee(Employee employee){
        db = DBOpenHelper.getHelper().getWritableDatabase();
        long rowid = -1;
        try {
            ContentValues cv = getEmployeeContentValues(employee);
            Logger.d("db更新employee:" + employee.getName());
            rowid = db.update(DBOpenHelper.TAB_EMPLOYEE, cv, "id = ? ", new String[]{employee.getId()});
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return rowid;
    }

    public void delAllEmployee() {
        String sql = "delete from tab_employee";
        db = DBOpenHelper.getHelper().getWritableDatabase();
        db.execSQL(sql);
        refresh();
    }

    /**
     * 删除已经不存在的员工信息
     * @param employeeList
     */
    public void delNonExistent(List<Employee> employeeList){
//        StringBuffer buffer = new StringBuffer();
//        for(int i = 0; i < employeeList.size(); i++){
//            buffer.append(employeeList.get(i).getId());
//            if(i < employeeList.size() - 1){
//                buffer.append(",");
//            }
//        }
        db = DBOpenHelper.getHelper().getWritableDatabase();
        for (int i = 0; i < employeeList.size(); i++) {
            Employee employee = employeeList.get(i);
            Logger.d("db删除employee：" + employee.getName());
            //删除SQL语句
            String sql = "delete from tab_employee where id = '" + employee.getId() + "'";
            //执行SQL语句
            db.execSQL(sql);
        }
        refresh();
    }

    public void delEmployee(Employee employee){
        db = DBOpenHelper.getHelper().getWritableDatabase();
        Logger.d("db删除：" + employee.getName());
        //删除SQL语句
        String sql = "delete from tab_employee where id = '" + employee.getId() + "'";
        //执行SQL语句
        db.execSQL(sql);
        refresh();
    }

    private List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        // 第一个参数:从第几条开始，第二个参数:每页显示几条
        String sql = "select * from tab_employee";
        Cursor c = null;
        try {
            db = DBOpenHelper.getHelper().getReadableDatabase();
            c = db.rawQuery(sql, new String[]{});
            while (c.moveToNext()) {
                Employee employee = new Employee();
                employee.setId(c.getString(c.getColumnIndex(DBOpenHelper.EMPLOYEE_ID)));
                employee.setName(c.getString(c.getColumnIndex(DBOpenHelper.EMPLOYEE_NAME)));
                employee.setFeature(c.getString(c.getColumnIndex(DBOpenHelper.EMPLOYEE_FEATURE)));
                employee.setImagePath(c.getString(c.getColumnIndex(DBOpenHelper.EMPLOYEE_IMAGEPATH)));
                employee.setImageUrl(c.getString(c.getColumnIndex(DBOpenHelper.EMPLOYEE_IMAGEURL)));

                employees.add(employee);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return employees;
    }

    public List<Employee> getEmployeesByIds(List<Employee> employeeList) {
        List<Employee> employees = new ArrayList<>();
        StringBuffer buffer = new StringBuffer("(");
        for(int i =0; i < employeeList.size(); i++){
            buffer.append("'").append(employeeList.get(i).getId()).append("'");
            if(i < employeeList.size() - 1){
                buffer.append(",");
            }
        }
        buffer.append(")");
        String sql = "select * from tab_employee where id in " + buffer.toString();
        Cursor c = null;
        try {
            db = DBOpenHelper.getHelper().getReadableDatabase();
            c = db.rawQuery(sql, new String[]{});
            while (c.moveToNext()) {
                Employee employee = new Employee();
                employee.setId(c.getString(c.getColumnIndex(DBOpenHelper.EMPLOYEE_ID)));
                employee.setName(c.getString(c.getColumnIndex(DBOpenHelper.EMPLOYEE_NAME)));
                employee.setFeature(c.getString(c.getColumnIndex(DBOpenHelper.EMPLOYEE_FEATURE)));
                employee.setImagePath(c.getString(c.getColumnIndex(DBOpenHelper.EMPLOYEE_IMAGEPATH)));
                employee.setImageUrl(c.getString(c.getColumnIndex(DBOpenHelper.EMPLOYEE_IMAGEURL)));

                employees.add(employee);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return employees;
    }

    private void refresh(){
        // employeeList.clear();
        // employeeList.addAll(getAllEmployees());
        // 只是把指针变一下，原来已经获取并且在使用的调用者不受影响，如果用clear和addAll的方法，正在调用的掉用者会受影响
        employeeList = getAllEmployees();
    }

    /**
     * 获取ContentValues,只填充不为空的项
     *
     * @param e
     * @return
     */
    private ContentValues getEmployeeContentValues(Employee e) {
        ContentValues cv = new ContentValues();
        if (!TextUtils.isEmpty(e.getId())) {
            cv.put(DBOpenHelper.EMPLOYEE_ID, e.getId());
        }
        if (!TextUtils.isEmpty(e.getName())) {
            cv.put(DBOpenHelper.EMPLOYEE_NAME, e.getName());
        }
        if (!TextUtils.isEmpty(e.getFeature())) {
            cv.put(DBOpenHelper.EMPLOYEE_FEATURE, e.getFeature());
        }
        cv.put(DBOpenHelper.EMPLOYEE_IMAGEPATH, e.getImagePath());
        cv.put(DBOpenHelper.EMPLOYEE_IMAGEURL, e.getImageUrl());

        return cv;
    }

    public List<Employee> getEmployeeList() {
        // 给一个克隆的，免得正在使用的时候原始数据发生变化了
//        List<Employee> employees = new ArrayList<>();
//        for(int i = 0; i < employeeList.size(); i++){
//            employees.add(employeeList.get(i).clone());
//        }
        return employeeList;
    }

    public List<Employee> getRealTimeEmployees() {
        List<Employee> employees = new ArrayList<>();
        // 第一个参数:从第几条开始，第二个参数:每页显示几条
        String sql = "select * from tab_employee";
        Cursor c = null;
        try {
            db = DBOpenHelper.getHelper().getReadableDatabase();
            c = db.rawQuery(sql, new String[] {});
            while (c.moveToNext()) {
                Employee employee = new Employee();
                employee.setId(c.getString(c.getColumnIndex(DBOpenHelper.EMPLOYEE_ID)));
                employee.setName(c.getString(c.getColumnIndex(DBOpenHelper.EMPLOYEE_NAME)));
                employee.setFeature(c.getString(c.getColumnIndex(DBOpenHelper.EMPLOYEE_FEATURE)));
                employee.setImagePath(
                        c.getString(c.getColumnIndex(DBOpenHelper.EMPLOYEE_IMAGEPATH)));
                employee.setImageUrl(c.getString(c.getColumnIndex(DBOpenHelper.EMPLOYEE_IMAGEURL)));

                employees.add(employee);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return employees;
    }
}
