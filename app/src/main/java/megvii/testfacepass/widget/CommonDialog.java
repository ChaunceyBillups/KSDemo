package megvii.testfacepass.widget;

import megvii.testfacepass1.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.NonNull;

/**
 * 确认删除dialog
 */
public class CommonDialog extends Dialog implements View.OnClickListener {

    private BtnClickListener btnClickListener;
    private int position, rightColor;
    private String title, content, left, right;
    private Context mCtx;

    public CommonDialog(@NonNull Context context) {
        super(context, R.style.FullscreenDialog);
        mCtx = context;
        // initWindow();
    }

    public CommonDialog(@NonNull Context context, String title, String content, String left, String right,
                        BtnClickListener
            btnClickListener) {
        super(context, R.style.customDialog);
        mCtx = context;
        this.title = title;
        this.left = left;
        this.right = right;
        this.btnClickListener = btnClickListener;
        this.content = content;
        // initWindow(); 这个initWindow方法会引起dialog的抖动，也没实际意义，去掉
    }

//    public CommonDialog(@NonNull Context context, Version version, String title, String content, String right, int rightColor) {
//        super(context, R.style.FullscreenDialog);
//        mCtx = context;
//        this.version = version;
//        this.title = title;
//        this.content = content;
//        this.right = right;
//        this.rightColor = rightColor;
//        initWindow();
//    }


    private void initWindow() {
        Window win = getWindow();
        assert win != null;
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        win.setAttributes(lp);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_common);
        initView();

    }

    private void initView() {
        ((TextView) findViewById(R.id.tv_content)).setText(content);
        if (!TextUtils.isEmpty(title))
            ((TextView) findViewById(R.id.tv_title)).setText(title);

        TextView tvLeft = findViewById(R.id.tv_left);
        tvLeft.setOnClickListener(this);
        if (!TextUtils.isEmpty(left)) {
            tvLeft.setText(left);
        }
        TextView tvRight = findViewById(R.id.tv_right);
        tvRight.setOnClickListener(this);
        if (!TextUtils.isEmpty(right)) {
            tvRight.setText(right);
            // tvRight.setTextColor(mCtx.getResources().getColor(rightColor));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_left:
                btnClickListener.onNegativeClick();
                dismiss();
                break;
            case R.id.tv_right:
                btnClickListener.onPositiveClick();
                dismiss();
                break;
        }

    }

    public interface BtnClickListener {
        // 确认点击回调
        void onNegativeClick();
        void onPositiveClick();
    }

}
