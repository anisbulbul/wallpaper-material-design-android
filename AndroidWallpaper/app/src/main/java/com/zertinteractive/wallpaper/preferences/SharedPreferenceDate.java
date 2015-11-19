package com.zertinteractive.wallpaper.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class SharedPreferenceDate {

    public static final String PREFS_NAME = "MOOD_WALLPAPER_DATE2";
    public static final String DATE = "dateStore2";

    public SharedPreferenceDate() {
        super();
    }


    public void storeDate(Context context, String date) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        editor = settings.edit();
        editor.clear();

        editor.putString(DATE, date);

        editor.commit();

    }

    public String loadDate(Context context) {
        SharedPreferences settings;
        String date = "LOAD ERROR ... ";
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (settings.contains(DATE)) {
            date = settings.getString(DATE, null);
        } else
            return null;
        return date;
    }
}
