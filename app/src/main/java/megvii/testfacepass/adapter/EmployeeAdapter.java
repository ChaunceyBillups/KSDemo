package megvii.testfacepass.adapter;

import static megvii.testfacepass.utils.Constants.GROUP_NAME;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import megvii.testfacepass.App;
import megvii.testfacepass1.R;
import megvii.testfacepass.db.EmployeeDBTool;
import megvii.testfacepass.model.Employee;
import megvii.testfacepass.utils.Constants;
import megvii.testfacepass.utils.Systems;
import megvii.testfacepass.utils.TaskUtils;
import megvii.testfacepass.utils.ToastHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import megvii.facepass.FacePassException;

public class EmployeeAdapter extends BaseAdapter {

    private Context context;
    private List<Employee> employeeList = new ArrayList<>();
    private BitmapFactory.Options option = new BitmapFactory.Options();



    public EmployeeAdapter(Context context, List<Employee> employeeList){
        super();
        this.context = context;
        this.employeeList = employeeList;
        option.inSampleSize = 8;
        option.inJustDecodeBounds = false;
    }

    @Override
    public int getCount() {
        return employeeList.size();
    }

    @Override
    public Object getItem(int position) {
        return employeeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Employee employee = employeeList.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_employee, null);
            viewHolder.name = convertView.findViewById(R.id.name);
            viewHolder.photo = convertView.findViewById(R.id.photo);
            viewHolder.delBtn = convertView.findViewById(R.id.btn_delemployee);
            viewHolder.delBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if(!TextUtils.isEmpty(employee.getFeature())){
                            App.getInstance().mFacePassHandler.unBindGroup(GROUP_NAME, employee.getFeature().getBytes());
                            App.getInstance().mFacePassHandler.deleteFace(employee.getFeature().getBytes());
                            employeeList.remove(position);

                            EmployeeDBTool.getInstance().delEmployee(employee);

                            ToastHelper.showMessage(context, "删除成功");
                        }
                    } catch (FacePassException e) {
                        e.printStackTrace();
                        ToastHelper.showMessage(context, "删除失败, 请查看日志");
                    }

                    notifyDataSetChanged();
                    // 删除sd卡的空目录
                    TaskUtils.runOnThreadPool(new Runnable() {
                        @Override
                        public void run() {
                            String fileRootPath = Constants.SDPATH;
                            File file = new File(fileRootPath);
                            Systems.clearDirs(file);
                        }
                    });
                }
            });

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        viewHolder.name.setText(employee.getName());
        viewHolder.photo.setImageBitmap(BitmapFactory.decodeFile(employee.getImagePath(), option));

        return convertView;
    }


    private static class ViewHolder{
        public TextView name;
        public Button delBtn;
        public ImageView photo;
        public Bitmap mBitmap;
    }
}


