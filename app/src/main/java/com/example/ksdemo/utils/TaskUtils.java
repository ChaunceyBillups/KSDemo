package com.example.ksdemo.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * 任务处理工具
 * Created by Tony on 2017/8/4.
 */
public class TaskUtils {
    public static final int THREAD_POOL_SIZE = 3;

    private static Handler mHandler = null;
    private static ExecutorService singleExecutor = null;
    private static ExecutorService poolExecutor = null;

    private TaskUtils() {

    }

    /**
     * 主线程handler
     *
     * @return
     */
    public static Handler handler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        } else {
            if (mHandler.getLooper() != Looper.getMainLooper()) {
                mHandler = new Handler(Looper.getMainLooper());
            }
        }
        return mHandler;
    }

    /**
     * 主线程执行
     *
     * @param runnable
     * @return
     */
    public static boolean runOnUiThread(Runnable runnable) {
        return handler().post((new RunnableAdapter(runnable)));
    }

    /**
     * 单线程列队执行
     *
     * @param runnable
     * @return
     */
    public static Future<?> runOnQueue(Runnable runnable) {
        if (singleExecutor == null) {
            singleExecutor = Executors.newSingleThreadExecutor();
        }
        return singleExecutor.submit((new RunnableAdapter(runnable)));
    }

    /**
     * 多线程执行
     *
     * @param runnable
     * @return
     */
    public static Future<?> runOnThreadPool(Runnable runnable) {
        if (poolExecutor == null) {
            poolExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        }
        return poolExecutor.submit(new RunnableAdapter(runnable));
    }

    /**
     * Runnable Adapter
     */
    public static class RunnableAdapter implements Runnable {
        private Runnable runnable;

        public RunnableAdapter(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            Benchmark.start("runnable[" + Thread.currentThread().getId() + "]");
            try {
                runnable.run();
            } catch (Throwable e) {
                Log.e("RunnableAdapter", "running task occurs exception:", e);
            } finally {
                Benchmark.end("runnable[" + Thread.currentThread().getId() + "]");
            }
        }
    }
}
