package megvii.testfacepass.widget;

import megvii.testfacepass1.R;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;

/**
 * loading加载等待
 * Created by siren on 2017/8/11.
 */
public class LoadingDialog extends Dialog {

    private String message;

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.FullscreenDialog);
        initWindow();
    }

    public void setMessage(String message) {
        this.message = message;
    }

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
        setContentView(R.layout.dialog_loading);
        View rootView = findViewById(R.id.container);

        ImageView ivLoading = findViewById(R.id.iv_loading);
        MaterialProgressDrawable mFooterProgress = new MaterialProgressDrawable(getContext(), rootView);
        mFooterProgress.setAlpha(255);
        mFooterProgress.setBackgroundColor(Color.TRANSPARENT);
        Resources resources = getContext().getResources();
        int color = resources.getColor(R.color.colorPrimaryDark);
        int blue = resources.getColor(R.color.colorPrimaryDark);
        int green = resources.getColor(R.color.colorPrimaryDark);
        mFooterProgress.setColorSchemeColors(color, blue, green);
        ivLoading.setImageDrawable(mFooterProgress);
        mFooterProgress.start();
    }

    private static LoadingDialog loadingDialog = null;

    public static void showLoading(Context context, String message) {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(context);
        }
        loadingDialog.setMessage(message);
        loadingDialog.show();
    }

    public static void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    @Override
    public void show() {
        super.show();
        ((TextView) findViewById(R.id.tv_msg)).setText(message);
    }

    @Override
    public void onBackPressed() {

    }
}