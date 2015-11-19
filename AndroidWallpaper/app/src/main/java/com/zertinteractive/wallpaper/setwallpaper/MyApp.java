package com.zertinteractive.wallpaper.setwallpaper;

import android.graphics.Bitmap;

import com.zertinteractive.wallpaper.library.util.ExceptionHandler;
import com.zertinteractive.wallpaper.library.util.LogCategory;

public class MyApp extends android.app.Application {
    private static final LogCategory log = new LogCategory("MyApp");
    private static final boolean debug = false;

    public static final Bitmap.Config getBitmapConfig(Bitmap image, Bitmap.Config defval) {
        if (image != null) {
            Bitmap.Config config = image.getConfig();
            if (config != null) return config;
        }
        return defval;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (debug) log.d("onCreate");

        ExceptionHandler.regist();
        Thread.yield();
    }
}
