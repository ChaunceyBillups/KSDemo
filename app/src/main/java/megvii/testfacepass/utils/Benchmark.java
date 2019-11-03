package megvii.testfacepass.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import android.util.Log;

/**
 * 一个工具类，可以用来跟踪程序效率问题
 * 目前暂时支持多线程，如果没有end时，会有LRU替换掉
 * 部分手机warn以上信息被屏蔽，可设置log为error
 * Created by Tony on 2017/8/4.
 */
public class Benchmark {

    private static final Map<String, BenchEntry> mBenchStack = new LinkedHashMap<String, BenchEntry>(101, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Entry<String, BenchEntry> eldest) {
            return size() > 100;
        }
    };

    private Benchmark() {
    }

    /**
     * 开始标记
     *
     * @param tag
     */
    public static void start(String tag) {
        BenchEntry entry = new BenchEntry();
        entry.tag = tag;
        entry.depth = mBenchStack.size();
        entry.start = System.currentTimeMillis();
        mBenchStack.put(tag, entry);
    }

    /**
     * 结束标记
     *
     * @param tag
     */
    public static void end(String tag) {
        BenchEntry entry = mBenchStack.get(tag);
        if (entry == null) {
            Log.w("Benchmark end", "Benchmark Not Match, tag spell mistake or forgot Benchmark.end(tag) invoke at somewhere ??");
            return;
        }
        entry.end = System.currentTimeMillis();

        long used = entry.end - entry.start;
        // Log.e("Benchmark end", "Benchmark [ " + entry.tag + " ] - Used: " + (used) + " ms. ");

        mBenchStack.remove(tag);
    }

    private static class BenchEntry {
        public long depth;
        public long start;
        public long end;
        public String tag;
    }
}
