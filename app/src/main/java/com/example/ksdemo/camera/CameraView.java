package com.example.ksdemo.camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.example.ksdemo.App;
import com.example.ksdemo.R;
import com.example.ksdemo.utils.Constants;
import com.example.ksdemo.utils.FaceConfig;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * 相机View
 */
public class CameraView extends SurfaceView implements Camera.AutoFocusCallback {

    private final static String TAG = "CameraView";
    private final static boolean DEBUG = true;
    private final static int SCALE_TYPE_4_3 = 1; // 自定义属性中4:3比例的枚举对应的值为1
    private final static int SCALE_TYPE_16_9 = 2; // 自定义属性中16:9比例的枚举对应的值为2

    private Camera mCamera; // 相机对象
    private Matrix mMatrix = new Matrix(); // 记录屏幕拉伸的矩阵，用于绘制人脸框使用
    private PreviewCallback mPreviewCallback; // 相机预览的数据回调
    private Size mPreviewSize; // 当前预览分辨率大小

    private float mPreviewScale; // 预览显示的比例(4:3/16:9)
    private float mPreviewScaleX;
    private float mPreviewScaleY;
    // 分辨率大小，以预览高度为标准(320, 480, 720, 1080...)
    private int mResolution;
    // 摄像头方向
    private int mCameraFacing;
    private boolean mIsOpened;

    public int mPreviewWidth; // 预览宽度
    public int mPreviewHeight; // 预览高度
    public int mDegrees; // 预览显示的角度
    public byte[] mPreviewBuffer; // 预览缓冲数据，使用可以让底层减少重复创建byte[]，起到重用的作用


    public int minWidth, minHeight;
    public int manualWidth, manualHeight;

    private Rect framingRect;
    private Rect framingRectInPreview;
    private Point screenResolution;
    private Point cameraResolution;
    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
    private static final int MAX_FRAME_HEIGHT = 675; // = 5/8 * 1080


    public CameraView(Context context) {
        super(context);
        init(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // 设置屏幕常亮
        setKeepScreenOn(true);
        // 自定义属性
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CameraView);
        if(FaceConfig.DEVICE_TYPE == Constants.DEVICETYPE_PHONE){
            mCameraFacing = a.getInt(R.styleable.CameraView_cameraFacing, Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else if (FaceConfig.DEVICE_TYPE == Constants.DEVICETYPE_QZ){
            mCameraFacing = a.getInt(R.styleable.CameraView_cameraFacing, Camera.CameraInfo.CAMERA_FACING_BACK);
        }

        mResolution = a.getInt(R.styleable.CameraView_resolution, 480);
        int scaleType = a.getInt(R.styleable.CameraView_scale, 0);
        mPreviewScale = getPreviewScale(scaleType);
        a.recycle();
        // 设置SurfaceHolder的回调
        getHolder().addCallback(callback);
    }

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (DEBUG) {
                Log.v(TAG, "SurfaceHolder Created");
            }
            try {
                openCamera(mCameraFacing); // 1.打开相机
                initParameters(); // 2.设置相机参数
                mCamera.setPreviewDisplay(getHolder()); // 3.设置预览显示的SurFace
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "打开相机失败, 请检查权限或者相机是否被占用", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (DEBUG) {
                Log.v(TAG, "SurfaceHolder Changed. width : " + width + ", height : " + height);
            }
            updateCamera(); // 4.更新相机属性，每次更换分辨率需要更新的操作，包括设置预览大小和方向，开始预览
            if (width > height) {
                mPreviewScaleX = width / (float) mPreviewWidth;
                mPreviewScaleY = height / (float) mPreviewHeight;
            } else {
                mPreviewScaleX = width / (float) mPreviewHeight;
                mPreviewScaleY = height / (float) mPreviewWidth;
            }
            mMatrix.setScale(mPreviewScaleX, mPreviewScaleY);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (DEBUG) {
                Log.v(TAG, "SurfaceHolder Destroyed");
            }
            releaseCamera(); // 5.释放相机资源
        }
    };

    private void openCamera(int mCameraFacing) throws RuntimeException {
        releaseCamera();
        Camera.CameraInfo info = new Camera.CameraInfo();
        // 0是RGB 1是IR
        int cameraId = 0;
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == mCameraFacing) {
                if(cameraId == 0){
                    mCamera = Camera.open(i); // 打开对应的摄像头，获取到camera实例
                } else if(cameraId == 1){
                    mCamera = Camera.open(i + 1); // 打开对应的摄像头，获取到camera实例
                }
                mIsOpened = true;
                return;
            }
        }
    }

    private void initParameters() {
        if (mCamera == null) {
            return;
        }
        try {

            // 这段是ks绘制人脸时需要的数据
            Size bestPreviewSize = getBestPreviewSize(mCamera);
            Log.i("metrics", "best height is" + bestPreviewSize.height + "width is " + bestPreviewSize.width);
            manualWidth = bestPreviewSize.width;
            manualHeight = bestPreviewSize.height;

            Parameters parameters = mCamera.getParameters();
            // 如果摄像头不支持这些参数都会出错的，所以设置的时候一定要判断是否支持
            List<String> supportedFlashModes = parameters.getSupportedFlashModes();
            if (supportedFlashModes != null && supportedFlashModes.contains(Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Parameters.FLASH_MODE_OFF); // 设置闪光模式
            }
            List<String> supportedFocusModes = parameters.getSupportedFocusModes();
            if (supportedFocusModes != null && supportedFocusModes.contains(Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO); // 设置聚焦模式
            }
            parameters.setPreviewFormat(ImageFormat.NV21); // 设置预览图片格式
            parameters.setPictureFormat(ImageFormat.JPEG); // 设置拍照图片格式
            parameters.setExposureCompensation(0);
            mCamera.setParameters(parameters); // 将设置好的parameters添加到相机里

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 每次启动摄像头、切换分辨率都需要进行的操作，所以抽离出来作为一个单独的方法
     */
    private void updateCamera() {
        if (mCamera == null) {
            return;
        }
        // mCamera.stopFaceDetection();
        mCamera.stopPreview(); // 1.先停止预览
        setCameraDisplayOrientation((Activity) getContext(), mCamera); // 2.设置相机的显示方向
        initPreviewSize(); // 3.初始化相机预览尺寸
        initPreviewBuffer(); // 4.初始化相机预览的缓存
        mCamera.startPreview(); // 5.开始预览
//        mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener(){
//            @Override
//            public void onFaceDetection(Camera.Face[] faces, Camera camera) {
//                if(faceView != null){
//                    Camera.Face maxFace = null;
//                    for (int i = 0; i < faces.length; i++) {
//                        if (maxFace == null || faces[i].rect.right - faces[i].rect.left > maxFace.rect.right - maxFace.rect.left) {
//                            maxFace = faces[i];
//                        }
//                    }
//                    Camera.Face[] newFaces = null;
//                    if(maxFace != null){
//                        newFaces = new Camera.Face[1];
//                        newFaces[0] = maxFace;
//                    }
//                    faceView.setFaces(newFaces);
//                }
//            }
//        });
//        mCamera.startFaceDetection();
    }

    /**
     * 初始化预览尺寸大小并设置，根据拉伸比例、分辨率来计算
     */
    private void initPreviewSize() {
        if (mCamera == null) {
            return;
        }
        Parameters parameters = mCamera.getParameters();
        mPreviewSize = getFitPreviewSize(parameters); // 获取适合的预览大小
        mPreviewWidth = mPreviewSize.width;
        mPreviewHeight = mPreviewSize.height;
        App.getInstance().cameraPreviewWidth = mPreviewWidth;
        App.getInstance().cameraPreviewHeight = mPreviewHeight;
        parameters.setPreviewSize(mPreviewWidth, mPreviewHeight); // 设置预览图片大小
        if (DEBUG) {
            Log.d(TAG, "initPreviewSize() mPreviewWidth: " + mPreviewWidth + ", mPreviewHeight: " + mPreviewHeight);
        }
        mCamera.setParameters(parameters);
    }

    /**
     * 具体计算最佳分辨率大小的方法
     */
    private Size getFitPreviewSize(Parameters parameters) {
        List<Size> previewSizes = parameters.getSupportedPreviewSizes(); // 获取支持的预览尺寸大小
        int minDelta = Integer.MAX_VALUE; // 最小的差值，初始值应该设置大点保证之后的计算中会被重置
        int index = 0; // 最小的差值对应的索引坐标
        for (int i = 0; i < previewSizes.size(); i++) {
            Size previewSize = previewSizes.get(i);
            if (DEBUG) {
                Log.d(TAG, "SupportedPreviewSize, width: " + previewSize.width + ", height: " + previewSize.height);
            }
            // 找到一个与设置的分辨率差值最小的相机支持的分辨率大小
            if (previewSize.width * mPreviewScale == previewSize.height) {
                int delta = Math.abs(mResolution - previewSize.height);
                if (delta == 0) {
                    return previewSize;
                }
                if (minDelta > delta) {
                    minDelta = delta;
                    index = i;
                }
            }
        }
        return previewSizes.get(index); // 默认返回与设置的分辨率最接近的预览尺寸
    }

    private void initPreviewBuffer() {
        if (mCamera == null) {
            return;
        }
        mPreviewBuffer = new byte[mPreviewWidth * mPreviewHeight * 3 / 2]; // 初始化预览缓冲数据的大小
        if (DEBUG) {
            Log.d(TAG, "initPreviewBuffer() mPreviewBuffer.length: " + mPreviewBuffer.length);
        }
        mCamera.addCallbackBuffer(mPreviewBuffer); // 将此预览缓冲数据添加到相机预览缓冲数据队列里
        mCamera.setPreviewCallbackWithBuffer(mPreviewCallback); // 设置预览的回调
    }

    /**
     * 设置相机显示的方向，必须设置，否则显示的图像方向会错误
     */
    private void setCameraDisplayOrientation(Activity activity, Camera camera) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0: // portrait
                mDegrees = 90;
                break;
            case Surface.ROTATION_90: // landscape
                mDegrees = 0;
                break;
            case Surface.ROTATION_180: // portrait-reverse
                mDegrees = 270;
                break;
            case Surface.ROTATION_270: // landscape-reverse
                mDegrees = 180;
                break;
            default:
                mDegrees = 90; // 大部分使用场景都是portrait，默认使用portrait的显示方向
                break;
        }
        camera.setDisplayOrientation(mDegrees);
    }

    protected void setExposure(int value) {
        Parameters parameters = mCamera.getParameters();
        if (value == parameters.getExposureCompensation()) {
            return;
        }
        Log.d(TAG, "exposure value : " + value);
        parameters.setExposureCompensation(value);
        mCamera.setParameters(parameters);
    }

    /**
     * 释放相机资源
     */
    public void releaseCamera() {
        if (null != mCamera) {
            if (DEBUG) {
                Log.v(TAG, "releaseCamera()");
            }
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopFaceDetection();
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mIsOpened = false;
        }
    }

    /**
     * 根据自定义属性的模式（4:3模式,16:9模式,auto模式）来获取相机的显示比例
     */
    private float getPreviewScale(int type) {
        if (type == SCALE_TYPE_4_3) { // 4:3模式
            return 0.75f;
        }
        if (type == SCALE_TYPE_16_9) { // 16:9模式
            return 0.5625f;
        }
        return getScreenScale(); // auto模式
    }

    /**
     * 获取设备屏幕的拉伸比例，目前安卓的设备屏幕比例大多是4：3和16：9两种
     */
    private float getScreenScale() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        float width = displayMetrics.widthPixels;
        float height = displayMetrics.heightPixels;
        float scale;
        if (width > height) {
            scale = height / width;
        } else {
            scale = width / height;
        }
        if (DEBUG) {
            Log.d(TAG, "displayMetrics.widthPixels : " + width);
            Log.d(TAG, "displayMetrics.heightPixels : " + height);
            Log.d(TAG, "scale : " + scale);
        }
        return Math.abs(scale - 0.75f) > Math.abs(scale - 0.5625f) ? 0.5625f : 0.75f; // 0.75(4:3) 或者 0.5625(16:9)
    }

    /**
     * 以点击的坐标点（基于CameraView控件大小的坐标系）为中心进行聚焦
     */
    private void focusOnPoint(int x, int y) {
        if (DEBUG) {
            Log.d(TAG, "touch point (" + x + ", " + y + ")");
        }
        if (mCamera == null) {
            return;
        }
        Parameters parameters = mCamera.getParameters();
        // 1.先要判断是否支持设置聚焦区域
        if (parameters.getMaxNumFocusAreas() > 0) {
            int width = getWidth();
            int height = getHeight();
            // 2.以触摸点为中心点，view窄边的1/4为聚焦区域的默认边长
            int length = Math.min(width, height) >> 3; // 1/8的长度
            int left = x - length;
            int top = y - length;
            int right = x + length;
            int bottom = y + length;
            // 3.映射，因为相机聚焦的区域是一个(-1000,-1000)到(1000,1000)的坐标区域
            left = left * 2000 / width - 1000;
            top = top * 2000 / height - 1000;
            right = right * 2000 / width - 1000;
            bottom = bottom * 2000 / height - 1000;
            // 4.判断上述矩形区域是否超过边界，若超过则设置为临界值
            left = left < -1000 ? -1000 : left;
            top = top < -1000 ? -1000 : top;
            right = right > 1000 ? 1000 : right;
            bottom = bottom > 1000 ? 1000 : bottom;
            if (DEBUG) {
                Log.d(TAG, "focus area (" + left + ", " + top + ", " + right + ", " + bottom + ")");
            }
            ArrayList<Camera.Area> areas = new ArrayList<Camera.Area>();
            areas.add(new Camera.Area(new Rect(left, top, right, bottom), 600));
            parameters.setFocusAreas(areas);
        }
        try {
            mCamera.cancelAutoFocus(); // 先要取消掉进程中所有的聚焦功能
            mCamera.setParameters(parameters);
            mCamera.autoFocus(this); // 调用聚焦
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPreviewCallback(PreviewCallback previewCallback) {
        mPreviewCallback = previewCallback;
    }

    /**
     * 每次预览的回调中，需要调用这个方法才可以起到重用mBuffer
     */
    public void addCallbackBuffer() {
        if (mCamera != null) {
            mCamera.addCallbackBuffer(mPreviewBuffer);
        }
    }

    /**
     * 切换前后摄像头
     */
    public void switchCamera() {
        mCameraFacing ^= 1; // 先改变摄像头朝向
        mIsOpened = false;
        restartCamera();
    }

    public void restartCamera() {
        if (mIsOpened) {
            return;
        }
        Log.w(TAG, "restartCamera");
        try {
            openCamera(mCameraFacing); // 重新打开对应的摄像头
            initParameters(); // 重新初始化参数
            mCamera.setPreviewDisplay(getHolder());
            updateCamera();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "打开相机失败, 请检查权限或者相机是否被占用", Toast.LENGTH_SHORT).show();
        }
    }

    public Camera getCamera() {
        return mCamera;
    }

    public List<Size> getSupportPreviewSize() {
        if (mCamera == null) {
            return null;
        }
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    public Matrix getMatrix() {
        return mMatrix;
    }

    public Size getPreviewSize() {
        return mPreviewSize;
    }

    public boolean isFrontCamera() {
        return mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    public void startPreview() {
        if (mCamera != null) {
            if (DEBUG) {
                Log.d(TAG, "startPreview()");
            }
            mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
            mCamera.startPreview();
        }
    }

    public void stopPreview() {
        if (mCamera != null) {
            if (DEBUG) {
                Log.d(TAG, "stopPreview()");
            }
            mCamera.stopPreview();
        }
    }

    public float getPreviewScaleX() {
        return mPreviewScaleX;
    }

    public float getPreviewScaleY() {
        return mPreviewScaleY;
    }

    /**
     * 设置长按可切换前后摄像头
     */
    public void setLongClickSwitchCamera() {
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                switchCamera();
                return true;
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);
        float scale; // 宽高比，用 较小数/较大数
        int finalWidth, finalHeight; // 根据预览的比例去重新计算和设置View的宽高
        if (originalWidth < originalHeight) {
            scale = originalWidth * 1.0f / originalHeight;
            if (scale == mPreviewScale) { // 比例一样则不改变
                finalWidth = originalWidth;
                finalHeight = originalHeight;
            } else {
                if (mPreviewScale == 0.75f) { // 预览比例4:3,压缩高度
                    finalWidth = originalWidth;
                    // 这个150是补上去的，不然底部的留白太大
                    finalHeight = finalWidth * 4 / 3 + 150;
                } else { // 预览比例16:9,压缩宽度
                    finalWidth = originalWidth;
                    finalHeight = finalWidth * 16 / 9;
                }
            }
        } else {
            scale = originalHeight * 1.0f / originalWidth;
            if (scale == mPreviewScale) { // 比例一样则不改变
                finalWidth = originalWidth;
                finalHeight = originalHeight;
            } else {
                if (mPreviewScale == 0.75f) { // 预览比例4:3,压缩宽度
                    finalHeight = originalHeight;
                    finalWidth = finalHeight * 4 / 3;
                } else { // 预览比例16:9,压缩高度
                    finalWidth = originalWidth;
                    finalHeight = finalWidth * 9 / 16;
                }
            }
        }
        if (DEBUG) {
            Log.d(TAG, "originalWidth :" + originalWidth + ", originalHeight :" + originalHeight);
            Log.d(TAG, "finalWidth: " + finalWidth + ", finalHeight: " + finalHeight);
        }
        setMeasuredDimension(finalWidth, finalHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                focusOnPoint((int) event.getX(), (int) event.getY()); // 点击聚焦
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (DEBUG) {
            Log.d(TAG, "onAutoFocus : " + success);
        }
    }

    private Size getBestPreviewSize(Camera mCamera) {
        Parameters camPara = mCamera.getParameters();
        List<Size> allSupportedSize = camPara.getSupportedPreviewSizes();
        ArrayList<Size> widthLargerSize = new ArrayList<Size>();
        int max = Integer.MIN_VALUE;
        Size maxSize = null;
        for (Size tmpSize : allSupportedSize) {
            int multi = tmpSize.height * tmpSize.width;
            if (multi > max) {
                max = multi;
                maxSize = tmpSize;
            }
            //选分辨率比较高的
            if (tmpSize.width > tmpSize.height && (tmpSize.width > minHeight / 2 || tmpSize.height > minWidth
                    / 2)) {
                widthLargerSize.add(tmpSize);
            }
        }
        if (widthLargerSize.isEmpty()) {
            widthLargerSize.add(maxSize);
        }

        final float propotion = minWidth >= minHeight ? (float) minWidth / (float) minHeight
                : (float) minHeight / (float) minWidth;

        Collections.sort(widthLargerSize, new Comparator<Size>() {
            @Override
            public int compare(Size lhs, Size rhs) {
                //                                int off_one = Math.abs(lhs.width * lhs.height - Screen.mWidth * Screen.mHeight);
                //                                int off_two = Math.abs(rhs.width * rhs.height - Screen.mWidth * Screen.mHeight);
                //                                return off_one - off_two;
                //选预览比例跟屏幕比例比较接近的
                float a = getPropotionDiff(lhs, propotion);
                float b = getPropotionDiff(rhs, propotion);
                return (int) ((a - b) * 10000);
            }
        });

        float minPropotionDiff = getPropotionDiff(widthLargerSize.get(0), propotion);
        ArrayList<Size> validSizes = new ArrayList<>();
        for (int i = 0; i < widthLargerSize.size(); i++) {
            Size size = widthLargerSize.get(i);
            float propotionDiff = getPropotionDiff(size, propotion);
            if (propotionDiff > minPropotionDiff) {
                break;
            }
            validSizes.add(size);
        }

        Collections.sort(validSizes, new Comparator<Size>() {
            @Override
            public int compare(Size lhs, Size rhs) {
                return rhs.width * rhs.height - lhs.width * lhs.height;
            }
        });
        return widthLargerSize.get(0);
    }

    private float getPropotionDiff(Size size, float standardPropotion) {
        return Math.abs((float) size.width / (float) size.height - standardPropotion);
    }


    /**
     * Calculates the framing rect which the UI should draw to show the user where to place the
     * barcode. This target helps with alignment as well as forces the user to hold the device
     * far enough away to ensure the image will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    public synchronized Rect getFramingRect() {
        if (framingRect == null) {
            if (mCamera == null) {
                return null;
            }
            // Point screenResolution = configManager.getScreenResolution();
            if (screenResolution == null) {
                // Called early, before init even finished
                return null;
            }

            int width = findDesiredDimensionInRange(screenResolution.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH);
            int height = findDesiredDimensionInRange(screenResolution.y, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT);

            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset = (screenResolution.y - height) / 2;
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
            Log.d(TAG, "Calculated framing rect: " + framingRect);
        }
        return framingRect;
    }


    private static int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
        int dim = 5 * resolution / 8; // Target 5/8 of each dimension
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }


    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview frame,
     * not UI / screen.
     *
     * @return {@link Rect} expressing barcode scan area in terms of the preview size
     */
    public synchronized Rect getFramingRectInPreview() {
        if (framingRectInPreview == null) {
            Rect framingRect = getFramingRect();
            if (framingRect == null) {
                return null;
            }
            Rect rect = new Rect(framingRect);
            // Point cameraResolution = configManager.getCameraResolution();
            // Point screenResolution = configManager.getScreenResolution();
            if (cameraResolution == null || screenResolution == null) {
                // Called early, before init even finished
                return null;
            }
//            rect.left = rect.left * cameraResolution.x / screenResolution.x;
//            rect.right = rect.right * cameraResolution.x / screenResolution.x;
//            rect.top = rect.top * cameraResolution.y / screenResolution.y;
//            rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y;
            rect.left = 0;
            rect.right = mPreviewWidth;
            rect.top = 0;
            rect.bottom = mPreviewHeight;
            framingRectInPreview = rect;
        }
        return framingRectInPreview;
    }


    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data A preview frame.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
//    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
//        Rect rect = getFramingRectInPreview();
//        if (rect == null) {
//            return null;
//        }
//        // Go ahead and assume it's YUV rather than die.
//        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
//                rect.width(), rect.height(), false);
//    }
//
//    public void setFaceview(FaceView faceView){
//        this.faceView = faceView;
//    }
}