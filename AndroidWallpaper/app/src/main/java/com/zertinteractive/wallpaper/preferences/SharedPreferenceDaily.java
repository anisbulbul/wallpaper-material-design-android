package com.zertinteractive.wallpaper.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;
import com.zertinteractive.wallpaper.viewmodels.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SharedPreferenceDaily {

    public static final String PREFS_NAME = "MOOD_WALLPAPER_DAILY";
    public static final String DAILY = "DailyWallpaper";

    public SharedPreferenceDaily() {
        super();
    }


    public void storeDaily(Context context, List<ViewModel> daily) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        editor = settings.edit();
        editor.clear();
        Gson gson = new Gson();
        String jsonDaily = gson.toJson(daily);

        editor.putString(DAILY, jsonDaily);

        editor.commit();

    }

    public ArrayList<ViewModel> loadDaily(Context context) {
        SharedPreferences settings;
        List<ViewModel> daily;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (settings.contains(DAILY)) {
            String jsonDaily = settings.getString(DAILY, null);
            Gson gson = new Gson();
            ViewModel[] dailyItems = gson.fromJson(jsonDaily, ViewModel[].class);
            daily = Arrays.asList(dailyItems);
            daily = new ArrayList<ViewModel>(daily);
        } else
            return null;
        return (ArrayList<ViewModel>) daily;
    }


    public void addDaily(Context context, ViewModel beanSampleList) {
        List<ViewModel> daily = loadDaily(context);
        if (daily == null)
            daily = new ArrayList<ViewModel>();
        daily.add(beanSampleList);
        storeDaily(context, daily);
    }

    public void removeDaily(Context context, ViewModel beanSampleList) {
        ArrayList<ViewModel> daily = loadDaily(context);
        if (daily != null) {
            daily.remove(beanSampleList);
            storeDaily(context, daily);
        }
    }


}
