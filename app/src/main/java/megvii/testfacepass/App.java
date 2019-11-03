package megvii.testfacepass;

import android.app.Application;
import megvii.facepass.FacePassHandler;

public class App extends Application {

    private static App instance;
    public FacePassHandler mFacePassHandler = null;
    public int cameraPreviewWidth;
    public int cameraPreviewHeight;
    public volatile boolean isPauseRecognize;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static App getInstance() {
        return instance;
    }
}
