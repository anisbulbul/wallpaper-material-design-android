package com.zertinteractive.wallpaper.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;
import com.zertinteractive.wallpaper.viewmodels.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SharedPreferenceFavourite {

    public static final String PREFS_NAME = "MOOD_WALLPAPER";
    public static final String FAVORITES = "FavoriteWallpaper";

    public SharedPreferenceFavourite() {
        super();
    }


    public void storeFavorites(Context context, List<ViewModel> favorites) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        editor = settings.edit();
        editor.clear();
        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);

        editor.putString(FAVORITES, jsonFavorites);

        editor.commit();

    }

    public ArrayList<ViewModel> loadFavorites(Context context) {
        SharedPreferences settings;
        List<ViewModel> favorites;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (settings.contains(FAVORITES)) {
            String jsonFavorites = settings.getString(FAVORITES, null);
            Gson gson = new Gson();
            ViewModel[] favoriteItems = gson.fromJson(jsonFavorites, ViewModel[].class);
            favorites = Arrays.asList(favoriteItems);
            favorites = new ArrayList<ViewModel>(favorites);
        } else
            return null;
        return (ArrayList<ViewModel>) favorites;
    }


    public void addFavorite(Context context, ViewModel beanSampleList) {
        List<ViewModel> favorites = loadFavorites(context);
        if (favorites == null)
            favorites = new ArrayList<ViewModel>();
        favorites.add(beanSampleList);
        storeFavorites(context, favorites);
    }

    public void removeFavorite(Context context, ViewModel beanSampleList) {
        ArrayList<ViewModel> favorites = loadFavorites(context);
        if (favorites != null) {
            favorites.remove(beanSampleList);
            storeFavorites(context, favorites);
        }
    }


}
