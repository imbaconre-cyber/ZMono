package app.zmono.font;

import android.app.Application;
import android.os.StrictMode;

public class ZMonoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Allow large allocations; largeHeap is set in manifest.
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
    }
}