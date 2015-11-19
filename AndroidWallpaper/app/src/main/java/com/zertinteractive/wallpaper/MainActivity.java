/*
 * Copyright (C) 2015 Antonio Leiva
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zertinteractive.wallpaper;

import android.Manifest;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nineoldandroids.animation.Animator;
import com.zertinteractive.wallpaper.activities.DetailActivity;
import com.zertinteractive.wallpaper.appstates.AppStatus;
import com.zertinteractive.wallpaper.categories.WallpaperCategory;
import com.zertinteractive.wallpaper.library.animations.Techniques;
import com.zertinteractive.wallpaper.library.animations.YoYo;
import com.zertinteractive.wallpaper.library.snackbar.SnackBar;
import com.zertinteractive.wallpaper.library.snackbar.SnackbarManager;
import com.zertinteractive.wallpaper.preferences.SharedPreferenceDaily;
import com.zertinteractive.wallpaper.preferences.SharedPreferenceDate;
import com.zertinteractive.wallpaper.preferences.SharedPreferenceFavourite;
import com.zertinteractive.wallpaper.recyclerview.RecyclerViewAdapter;
import com.zertinteractive.wallpaper.recyclerview.TextViewAdapter;
import com.zertinteractive.wallpaper.searchs.ImageResult;
import com.zertinteractive.wallpaper.searchs.Settings;
import com.zertinteractive.wallpaper.viewmodels.ViewModel;
import com.zertinteractive.wallpaper.viewmodels.ViewTextModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements RecyclerViewAdapter.OnItemClickListener,
        TextViewAdapter.OnItemClickListener {

    private Interpolator interpolator;
    private static final String LOG_TAG = "MAIN LOG CAT";
    private static final String EXTRA_IMAGE_BIG = "com.zertinteractive.wallpaper.extraImageBig";
    private static final int GRIDVIEW_COLUMN = 2;
    private static int reminderCount = 0;
    private static final int DELAY = 100;

    private static Context context;
    private DrawerLayout drawerLayout;
    private EditText searchKey;
    private ImageView searchQueryButton;
    private ImageView searchQueryClear;
    private AdView mAdView;
    private TimerTasks timerTasks;
    private ImageView imageView;
    private ProgressBar progressBar;

    private long enqueue;
    private DownloadManager dm;

    private FloatingActionButton fabSearchWallpaper;
    private static boolean isSutterOn = false;
    private static boolean isSearchOn = false;
    private static boolean isSearchScreenOpen = false;
    private static boolean isSearchAnimationRunning = false;
    private static boolean isSearchingRunning = false;

    private static String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

    // search components
    RecyclerView recyclerTextView;
    TextViewAdapter searchTextadapter;

    //    private View content;
    public static RecyclerView recyclerView;
    private LinearLayout searchView;
    private LinearLayout categoryLayout;
    private static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.anrosoft.zombiemission";
    private static List<ViewModel> allViewModel = new ArrayList<>();
    private static List<ViewModel> happyViewModel = new ArrayList<>();
    private static List<ViewModel> sadViewModel = new ArrayList<>();
    private static List<ViewModel> surpriseViewModel = new ArrayList<>();
    private static List<ViewModel> angryViewModel = new ArrayList<>();
    private static List<ViewModel> funnyViewModel = new ArrayList<>();
    private static List<ViewModel> amazedViewModel = new ArrayList<>();
    private static List<ViewModel> dailyViewModel = new ArrayList<>();
    private static List<ViewModel> featuredViewModel = new ArrayList<>();
    private static List<ViewModel> favouriteViewModel = new ArrayList<>();
    private static List<ViewModel> searchViewModel = new ArrayList<>();

    private static SharedPreferenceFavourite sharedPreferenceFavourite = new SharedPreferenceFavourite();
    private static SharedPreferenceDaily sharedPreferenceDaily = new SharedPreferenceDaily();
    private static SharedPreferenceDate sharedPreferenceDate = new SharedPreferenceDate();
    private static WallpaperCategory currentWallpaperCategory = WallpaperCategory.ALL;

    public static WallpaperCategory getCurrentWallpaperCategory() {
        return currentWallpaperCategory;
    }

    public static void setOneToEight(int position, int superCategory) {
        if (currentWallpaperCategory == WallpaperCategory.ALL) {
            allViewModel.get(position).setSuperCategory(superCategory);
        } else if (currentWallpaperCategory == WallpaperCategory.HAPPY) {
            happyViewModel.get(position).setSuperCategory(superCategory);
        } else if (currentWallpaperCategory == WallpaperCategory.SAD) {
            sadViewModel.get(position).setSuperCategory(superCategory);
        } else if (currentWallpaperCategory == WallpaperCategory.SURPRISE) {
            surpriseViewModel.get(position).setSuperCategory(superCategory);
        } else if (currentWallpaperCategory == WallpaperCategory.ANGRY) {
            angryViewModel.get(position).setSuperCategory(superCategory);
        } else if (currentWallpaperCategory == WallpaperCategory.FUNNY) {
            funnyViewModel.get(position).setSuperCategory(superCategory);
        } else if (currentWallpaperCategory == WallpaperCategory.AMAZED) {
            amazedViewModel.get(position).setSuperCategory(superCategory);
        } else if (currentWallpaperCategory == WallpaperCategory.DAILY) {
            dailyViewModel.get(position).setSuperCategory(superCategory);
        } else if (currentWallpaperCategory == WallpaperCategory.FEATURED) {
            featuredViewModel.get(position).setSuperCategory(superCategory);
        } else if (currentWallpaperCategory == WallpaperCategory.FAVOURITE) {
            favouriteViewModel.get(position).setSuperCategory(superCategory);
        } else if (currentWallpaperCategory == WallpaperCategory.SEARCH) {
            searchViewModel.get(position).setSuperCategory(superCategory);
        }
    }

    public static void resetWallpapers(int position, int superCategory, ViewModel itemViewModel) {

        Log.e("FAV", "P" + position + " : " + "S" + superCategory + " : " + "C" + currentWallpaperCategory + " : " + itemViewModel.getImageSmall());
        Log.e("SIZE : ", favouriteViewModel.size() + "");

        switch (superCategory) {
            case 3:
                if (currentWallpaperCategory != WallpaperCategory.FAVOURITE) {
                    setOneToEight(position, superCategory);
                    int i = 0;
                    for (i = 0; i < favouriteViewModel.size(); i++) {
                        if (favouriteViewModel.get(i).getImageSmall().equals(itemViewModel.getImageSmall())) {
                            break;
                        }
                    }
                    if (i == favouriteViewModel.size()) {
                        favouriteViewModel.add(0, itemViewModel);
                        Log.e("ADD : ", "ADD");
                    }
                    for (i = 0; 0 <= i && i < favouriteViewModel.size(); i++) {
                        if (favouriteViewModel.get(i).getSuperCategory() != 3) {
                            favouriteViewModel.remove(i);
                            i--;
                        }
                    }
                    sharedPreferenceFavourite.storeFavorites(context, favouriteViewModel);
                } else if (currentWallpaperCategory == WallpaperCategory.FAVOURITE) {
                    setOneToEight(position, superCategory);
                    sharedPreferenceFavourite.storeFavorites(context, favouriteViewModel);
                }
                break;
            case 0:
                if (currentWallpaperCategory != WallpaperCategory.FAVOURITE) {
                    setOneToEight(position, superCategory);
                    for (int i = 0; i < favouriteViewModel.size(); i++) {
                        if (favouriteViewModel.get(i).getImageSmall().equals(itemViewModel.getImageSmall())) {
                            Log.e("REMOVE : ", "REMOVE");
                            favouriteViewModel.remove(i);
                            break;
                        }
                    }
                    for (int i = 0; 0 <= i && i < favouriteViewModel.size(); i++) {
                        if (favouriteViewModel.get(i).getSuperCategory() != 3) {
                            favouriteViewModel.remove(i);
                            i--;
                        }
                    }
                    sharedPreferenceFavourite.storeFavorites(context, favouriteViewModel);
                } else if (currentWallpaperCategory == WallpaperCategory.FAVOURITE) {
                    setOneToEight(position, superCategory);
                    sharedPreferenceFavourite.storeFavorites(context, favouriteViewModel);
                }
                break;
            default:

                break;
        }
    }


    public static void initWallpaperLists() {

        allViewModel.clear();
        happyViewModel.clear();
        sadViewModel.clear();
        surpriseViewModel.clear();
        angryViewModel.clear();
        funnyViewModel.clear();
        amazedViewModel.clear();
        dailyViewModel.clear();
        featuredViewModel.clear();
        favouriteViewModel.clear();


        if (sharedPreferenceFavourite.loadFavorites(context) != null) {
            favouriteViewModel = sharedPreferenceFavourite.loadFavorites(context);
        }

        int i;
        for (i = 0; 0 <= i && i < favouriteViewModel.size(); i++) {
            favouriteViewModel.get(i).setWallpaperCategory(WallpaperCategory.FAVOURITE);
            if (favouriteViewModel.get(i).getSuperCategory() != 3) {
                favouriteViewModel.remove(i);
                i--;
            }
        }

        for (i = 1; i <= 641; i++) {
            String imageUrlSmall = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteAllHD/Small/CuteAllHD_Small_" + i + ".jpg";
            String imageUrlBig = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteAllHD/Big/CuteAllHD_Big_" + i + ".jpg";
            String imageUrlDownload = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteAllHD/Big/CuteAllHD_Big_" + i + ".jpg~original";
            int superCategoryOption = 0;
            for (int j = 0; j < favouriteViewModel.size(); j++) {
                if (imageUrlSmall.equals(favouriteViewModel.get(j).getImageSmall())) {
                    superCategoryOption = 3;
                    break;
                }
            }
            allViewModel.add(new ViewModel("Title " + i, WallpaperCategory.ALL, superCategoryOption, imageUrlSmall, imageUrlBig, imageUrlDownload));
//            Collections.shuffle(allViewModel);
        }
        for (i = 1; i <= 202; i++) {
            String imageUrlSmall = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteAnimHD/Small/CuteAnimHD_Small_" + i + ".jpg";
            String imageUrlBig = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteAnimHD/Big/CuteAnimHD_Big_" + i + ".jpg";
            String imageUrlDownload = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteAnimHD/Big/CuteAnimHD_Big_" + i + ".jpg~original";
            int superCategoryOption = 0;
            for (int j = 0; j < favouriteViewModel.size(); j++) {
                if (imageUrlSmall.equals(favouriteViewModel.get(j).getImageSmall())) {
                    superCategoryOption = 3;
                    break;
                }
            }
            happyViewModel.add(new ViewModel("2Title " + i, WallpaperCategory.HAPPY, superCategoryOption, imageUrlSmall, imageUrlBig, imageUrlDownload));
//            Collections.shuffle(happyViewModel);
        }
        for (i = 1; i <= 217; i++) {
            String imageUrlSmall = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteBabHD/Small/CuteBabHD_Small_" + i + ".jpg";
            String imageUrlBig = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteBabHD/Big/CuteBabHD_Big_" + i + ".jpg";
            String imageUrlDownload = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteBabHD/Big/CuteBabHD_Big_" + i + ".jpg~original";
            int superCategoryOption = 0;
            for (int j = 0; j < favouriteViewModel.size(); j++) {
                if (imageUrlSmall.equals(favouriteViewModel.get(j).getImageSmall())) {
                    superCategoryOption = 3;
                    break;
                }
            }
            sadViewModel.add(new ViewModel("2Title " + i, WallpaperCategory.SAD, superCategoryOption, imageUrlSmall, imageUrlBig, imageUrlDownload));
//            Collections.shuffle(sadViewModel);
        }
        for (i = 1; i <= 552; i++) {
            String imageUrlSmall = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteCartHD/Small/CuteCartHD_Small_" + i + ".jpg";
            String imageUrlBig = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteCartHD/Big/CuteCartHD_Big_" + i + ".jpg";
            String imageUrlDownload = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteCartHD/Big/CuteCartHD_Big_" + i + ".jpg~original";
            int superCategoryOption = 0;
            for (int j = 0; j < favouriteViewModel.size(); j++) {
                if (imageUrlSmall.equals(favouriteViewModel.get(j).getImageSmall())) {
                    superCategoryOption = 3;
                    break;
                }
            }
            surpriseViewModel.add(new ViewModel("2Title " + i, WallpaperCategory.SURPRISE, superCategoryOption, imageUrlSmall, imageUrlBig, imageUrlDownload));
//            Collections.shuffle(surpriseViewModel);
        }
        for (i = 1; i <= 200; i++) {
            String imageUrlSmall = "http://i1377.photobucket.com/albums/ah68/testzert/Energy%20Saving/All/Small/EnAll_Small_" + i + ".jpg";
            String imageUrlBig = "http://i1377.photobucket.com/albums/ah68/testzert/Energy%20Saving/All/Big/EnAll_Big_" + i + ".jpg";
            String imageUrlDownload = "http://i1377.photobucket.com/albums/ah68/testzert/Energy%20Saving/All/Big/EnAll_Big_" + i + ".jpg~original";
            int superCategoryOption = 0;
            for (int j = 0; j < favouriteViewModel.size(); j++) {
                if (imageUrlSmall.equals(favouriteViewModel.get(j).getImageSmall())) {
                    superCategoryOption = 3;
                    break;
                }
            }
            angryViewModel.add(new ViewModel("2Title " + i, WallpaperCategory.ANGRY, superCategoryOption, imageUrlSmall, imageUrlBig, imageUrlDownload));
//            Collections.shuffle(angryViewModel);
        }
        for (i = 1; i <= 198; i++) { //total 198
            String imageUrlSmall = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CutePatHD/Small/CutePatHD_Small_" + i + ".jpg";
//            String imageUrlSmall = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CutePatHD/Small/CutePatHD_Small_" + i + ".jpg";
            String imageUrlBig = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CutePatHD/Big/CutePatHD_Big_" + i + ".jpg";
            String imageUrlDownload = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CutePatHD/Big/CutePatHD_Big_" + i + ".jpg~original";
            int superCategoryOption = 0;
            for (int j = 0; j < favouriteViewModel.size(); j++) {
                if (imageUrlSmall.equals(favouriteViewModel.get(j).getImageSmall())) {
                    superCategoryOption = 3;
                    break;
                }
            }
            funnyViewModel.add(new ViewModel("2Title " + i, WallpaperCategory.FUNNY, superCategoryOption, imageUrlSmall, imageUrlBig, imageUrlDownload));
//            Collections.shuffle(funnyViewModel);
        }
        for (i = 1; i <= 200; i++) {
            String imageUrlSmall = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteRomHD/Small/CuteRomHD_Small_" + i + ".jpg";
            String imageUrlBig = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteRomHD/Big/CuteRomHD_Big_" + i + ".jpg";
            String imageUrlDownload = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteRomHD/Big/CuteRomHD_Big_" + i + ".jpg~original";
            int superCategoryOption = 0;
            for (int j = 0; j < favouriteViewModel.size(); j++) {
                if (imageUrlSmall.equals(favouriteViewModel.get(j).getImageSmall())) {
                    superCategoryOption = 3;
                    break;
                }
            }
            amazedViewModel.add(new ViewModel("2Title " + i, WallpaperCategory.AMAZED, superCategoryOption, imageUrlSmall, imageUrlBig, imageUrlDownload));
//            Collections.shuffle(amazedViewModel);
        }
        for (i = 1; i <= 200; i++) {
            String imageUrlSmall = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteFeat/Small/CuteFeat_Small_" + i + ".jpg";
            String imageUrlBig = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteFeat/Big/CuteFeat_Big_" + i + ".jpg";
            String imageUrlDownload = "http://i1377.photobucket.com/albums/ah68/testzert/CuteDemo/CuteFeat/Big/CuteFeat_Big_" + i + ".jpg~original";
            int superCategoryOption = 0;
            for (int j = 0; j < favouriteViewModel.size(); j++) {
                if (imageUrlSmall.equals(favouriteViewModel.get(j).getImageSmall())) {
                    superCategoryOption = 3;
                    break;
                }
            }
            featuredViewModel.add(new ViewModel("2Title " + i, WallpaperCategory.FEATURED, superCategoryOption, imageUrlSmall, imageUrlBig, imageUrlDownload));
//            Collections.shuffle(featuredViewModel);
        }

        date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        if (sharedPreferenceDate.loadDate(context) == null) {  // if date is not set
            sharedPreferenceDate.storeDate(context, date);
            getDailyViewModel(true);
            sharedPreferenceDaily.storeDaily(context, dailyViewModel);
        } else {// if date is set
            if (sharedPreferenceDaily.loadDaily(context) == null) {
                getDailyViewModel(true);
                sharedPreferenceDaily.storeDaily(context, dailyViewModel);
            } else {
                if (!sharedPreferenceDate.loadDate(context).equals(date)) {
                    dailyViewModel = getDailyViewModel(sharedPreferenceDaily.loadDaily(context));
                    sharedPreferenceDaily.storeDaily(context, dailyViewModel);
                } else {
                    dailyViewModel = sharedPreferenceDaily.loadDaily(context);
                }
            }
            sharedPreferenceDate.storeDate(context, date);
        }

        for (i = 0; i < dailyViewModel.size(); i++) {
            dailyViewModel.get(i).setWallpaperCategory(WallpaperCategory.DAILY);
            for (int j = 0; j < favouriteViewModel.size(); j++) {
                if (dailyViewModel.get(i).getImageSmall().equals(favouriteViewModel.get(j).getImageSmall())) {
                    dailyViewModel.get(i).setSuperCategory(3);
                    break;
                }
            }
        }
//        Collections.shuffle(dailyViewModel);
    }

    public static List<ViewModel> getDailyViewModel(List<ViewModel> mDailyViewModel) {
        dailyViewModel = mDailyViewModel;
        getDailyViewModel(false);
        return dailyViewModel.size() > 900 ? dailyViewModel.subList(dailyViewModel.size() - 900, dailyViewModel.size()) : dailyViewModel;
    }

    public static void getDailyViewModel(boolean isClearNeeded) {
        if (isClearNeeded) {
            dailyViewModel.clear();
        }
        for (int i = 0; i < 4; i++) {
            dailyViewModel.add(searchAndAddItem(allViewModel, dailyViewModel));
            dailyViewModel.add(searchAndAddItem(happyViewModel, dailyViewModel));
            dailyViewModel.add(searchAndAddItem(sadViewModel, dailyViewModel));
            dailyViewModel.add(searchAndAddItem(surpriseViewModel, dailyViewModel));
            dailyViewModel.add(searchAndAddItem(angryViewModel, dailyViewModel));
            dailyViewModel.add(searchAndAddItem(funnyViewModel, dailyViewModel));
            dailyViewModel.add(searchAndAddItem(amazedViewModel, dailyViewModel));
            dailyViewModel.add(searchAndAddItem(featuredViewModel, dailyViewModel));
        }
    }

    public static boolean containsItem(List<ViewModel> searchList, ViewModel searchItem) {
        for (int i = 0; i < searchList.size(); i++) {
            if ((searchList.get(i).getImageSmall()).equals(searchItem.getImageSmall())) {
                return true;
            }
        }
        return false;
    }

    public static ViewModel searchAndAddItem(List<ViewModel> viewModels, List<ViewModel> viewModelsDaily) {
        ViewModel viewModel = viewModels.get(Math.abs(new Random().nextInt(viewModels.size() - 1)));
        while (containsItem(viewModelsDaily, viewModel)) {
            viewModel = viewModels.get(Math.abs(new Random().nextInt(viewModels.size() - 1)));
        }
        return viewModel;
    }

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @SuppressWarnings("NewApi")
    public void checkWriteExternalPermission() {
        int res = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (res != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    public void initDownloadComponents() {

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    //
                }
            }
        };

        registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        unregisterReceiver(receiver);
    }


    private void downloadTestImage() {
        String path = Environment.getExternalStorageDirectory().toString();

        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse("http://pic2.pbsrc.com/footer/footer-Instagram.png"));

        File file = new File(path, "/" + DetailActivity.TEMP_WALLPAPER_DIR + "/" + DetailActivity.TEMP_WALLPAPER_NAME + ".png");
        if (file.exists()) {
            SnackbarManager.show(
                    SnackBar.with(this)
                            .text("IMAGE SETED : "));
        } else {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setDestinationInExternalPublicDir(DetailActivity.TEMP_WALLPAPER_DIR, DetailActivity.TEMP_WALLPAPER_NAME + ".png");
            enqueue = dm.enqueue(request);
            SnackbarManager.show(
                    SnackBar.with(this)
                            .text("IMAGE SETING : "));
        }
    }

    byte RC_PERMISSION_WRITE_EXTERNAL_STORAGE;

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RC_PERMISSION_WRITE_EXTERNAL_STORAGE);
        } else {
            // permission has been granted, continue as usual

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == RC_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to
            } else {
                // Permission was denied or request was cancelled
                finish();
            }
        }
    }

    private void initSearchGridView() {
        final String[] viewModelText = {
                "Android", "Amazed", "Others",
                "iOS", "Wedding", "Nice",
                "Men", "Love", "Women",
                "Lucky", "Funny"};
        List<ViewTextModel> viewTextModels = new ArrayList<>();
        viewTextModels.clear();
        for (int i = 0; i < viewModelText.length; i++) {
            viewTextModels.add(new ViewTextModel(viewModelText[i]));
        }

        recyclerTextView = (RecyclerView) findViewById(R.id.grades);
        recyclerTextView.setLayoutManager(new GridLayoutManager(this, 3));

        searchTextadapter = new TextViewAdapter(this, viewTextModels);
        searchTextadapter.setOnItemClickListener(this);
        recyclerTextView.setAdapter(searchTextadapter);
    }

    private void searchActionMethod() {
        if (!isSearchingRunning) {
            isSearchingRunning = true;
            progressBar.setVisibility(View.VISIBLE);
            searchViewModel.clear();
            searchKey.setEnabled(false);
            searchWithOffset(0, searchKey.getText().toString());
        }
    }


    public void searchWithOffset(final int offset, final String query) {
        AsyncHttpClient client = new AsyncHttpClient();
        Settings setting = new Settings();

//        String url = "https://ajax.googleapis.com/ajax/services/search/images?" + "start=" + Integer.toString(offset * 8) + "&v=1.0&q=" + Uri.encode(query) + "&rsz=8" + setting.getQueryString();
//        String url = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&safe=active&rsz=8&q=%@&start=%ld&&imgsz=", query, (long) (_urlCount * 8)]];

        final String url = "https://ajax.googleapis.com/ajax/services/search/images?start=" + (offset * 8) + "&v=1.0&safe=active&q=" + query + "&rsz=8";

        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                JSONArray imageJsonResults = null;
                try {
                    Log.d("SEARCH", "QUERY_DONE : " + url);
                    imageJsonResults = response.getJSONObject("responseData").getJSONArray("results");
                    List<ImageResult> imageResults = ImageResult.fromJSONArray(imageJsonResults);
                    for (int i = 0; i < imageResults.size(); i++) {
                        Log.e("SEARCH", "Thum : " + imageResults.get(i).getThumbUrl());
                        Log.e("SEARCH", "Down : " + imageResults.get(i).getFullUrl());
                        searchViewModel.add(new ViewModel(WallpaperCategory.SEARCH,
                                imageResults.get(i).getThumbUrl(),
                                imageResults.get(i).getFullUrl(),
                                imageResults.get(i).getFullUrl()));
                    }
                    if (offset < 5) {
                        searchWithOffset(offset + 1, query);
                    } else {
                        setRecyclerAdapter(recyclerView, 10);
                        progressBar.setVisibility(View.GONE);
                        currentWallpaperCategory = WallpaperCategory.SEARCH;
                        isSearchingRunning = false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(LOG_TAG, "onCreate()");
        setContentView(R.layout.activity_main);
        context = this;
        checkPermission();
        DetailActivity.setContext(this);
        initDownloadComponents();
        downloadTestImage();
        initSearchGridView();

        progressBar = (ProgressBar) findViewById(R.id.progressBarImageSearch);
        progressBar.setVisibility(View.GONE);

        searchQueryButton = (ImageView) findViewById(R.id.search_query_ok);
        searchQueryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(searchKey.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                searchActionMethod();
            }
        });

        searchKey = (EditText) findViewById(R.id.search_key);
        searchKey.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
//                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    in.hideSoftInputFromWindow(searchKey.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//                    Log.e("SEARCH", searchKey.getText().toString());
//                }
                searchActionMethod();
                return false;
            }
        });

        searchQueryClear = (ImageView) findViewById(R.id.search_query_clear);
        searchQueryClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchKey.setText("");
            }
        });

        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setVisibility(View.GONE);

//        mAdView.setScaleX(1.3f);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();


        // Start loading the ad in sthe background.
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });


//        AdView mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);


        initWallpaperLists();
        initRecyclerView(GRIDVIEW_COLUMN);
        initSearchView();
        initFab();
        initToolbar();
        setupDrawerLayout();
        setCategorySutter(false);
        categorySutterChange(1200);
//        setNotification();

        setupWindowAnimations();

        reminderCount = 0;

        timerTasks = new TimerTasks(context);

        timerTasks.startTimerTask();


        currentWallpaperCategory = WallpaperCategory.ALL;
        DetailActivity.checkDir();


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            setRecyclerAdapter(recyclerView, 0);
            currentWallpaperCategory = WallpaperCategory.ALL;
        }


        LinearLayout categoryHappyWallpaper = (LinearLayout) findViewById(R.id.category_happy);
        LinearLayout categorySadWallpaper = (LinearLayout) findViewById(R.id.category_sad);
        LinearLayout categorySurpriseWallpaper = (LinearLayout) findViewById(R.id.category_surprise);
        LinearLayout categoryAngryWallpaper = (LinearLayout) findViewById(R.id.category_angry);
        LinearLayout categoryFunnyWallpaper = (LinearLayout) findViewById(R.id.category_funny);
        LinearLayout categoryAmazedWallpaper = (LinearLayout) findViewById(R.id.category_amazed);
        categoryHappyWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRecyclerAdapter(recyclerView, 1);
                currentWallpaperCategory = WallpaperCategory.HAPPY;
            }
        });
        categorySadWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRecyclerAdapter(recyclerView, 2);
                currentWallpaperCategory = WallpaperCategory.SAD;
            }
        });
        categorySurpriseWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRecyclerAdapter(recyclerView, 3);
                currentWallpaperCategory = WallpaperCategory.SURPRISE;
            }
        });
        categoryAngryWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRecyclerAdapter(recyclerView, 4);
                currentWallpaperCategory = WallpaperCategory.ANGRY;
            }
        });
        categoryFunnyWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRecyclerAdapter(recyclerView, 5);
                currentWallpaperCategory = WallpaperCategory.FUNNY;
            }
        });
        categoryAmazedWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRecyclerAdapter(recyclerView, 6);
                currentWallpaperCategory = WallpaperCategory.AMAZED;
            }
        });


        ImageView category_daily = (ImageView) findViewById(R.id.category_daily);
        ImageView category_featured = (ImageView) findViewById(R.id.category_featured);
        ImageView category_favourite = (ImageView) findViewById(R.id.category_favourite);
        category_daily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRecyclerAdapter(recyclerView, 7);
                currentWallpaperCategory = WallpaperCategory.DAILY;
            }
        });
        category_featured.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRecyclerAdapter(recyclerView, 8);
                currentWallpaperCategory = WallpaperCategory.FEATURED;
            }
        });
        category_favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRecyclerAdapter(recyclerView, 9);
                currentWallpaperCategory = WallpaperCategory.FAVOURITE;
            }
        });

        LinearLayout categoryWallpaper = (LinearLayout) findViewById(R.id.categoryButtonArrow);
        LinearLayout categoryAllWallpaper = (LinearLayout) findViewById(R.id.categoryWallpaper);
        LinearLayout kidsyWallpaperAction = (LinearLayout) findViewById(R.id.kidsyWallpaperAction);
        LinearLayout keepCalmWallpaperAction = (LinearLayout) findViewById(R.id.keepCalmWallpaperAction);
        LinearLayout romanticWallpaperAction = (LinearLayout) findViewById(R.id.romanticWallpaperAction);
        LinearLayout materialWallpaperAction = (LinearLayout) findViewById(R.id.materialWallpaperAction);
        categoryAllWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRecyclerAdapter(recyclerView, 0);
                currentWallpaperCategory = WallpaperCategory.ALL;
            }
        });
        categoryWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categorySutterChange(600);
//                YoYo.with(Techniques.RotateAntiClockWise)
//                        .duration(300)
//                        .interpolate(new AccelerateDecelerateInterpolator())
//                        .withListener(new Animator.AnimatorListener() {
//                            @Override
//                            public void onAnimationStart(Animator animation) {
//
//                            }
//
//                            @Override
//                            public void onAnimationEnd(Animator animation) {
//
//                            }
//
//                            @Override
//                            public void onAnimationCancel(Animator animation) {
//                                Toast.makeText(MainActivity.this, "canceled", Toast.LENGTH_SHORT).show();
//                            }
//
//                            @Override
//                            public void onAnimationRepeat(Animator animation) {
//
//                            }
//                        })
//                        .playOn(findViewById(R.id.categoryButtonArrow));
            }
        });
        kidsyWallpaperAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUri(PLAY_STORE_URL);
            }
        });
        keepCalmWallpaperAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUri(PLAY_STORE_URL);
            }
        });
        romanticWallpaperAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUri(PLAY_STORE_URL);
            }
        });
        materialWallpaperAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUri(PLAY_STORE_URL);
            }
        });

//        Button button = (Button) findViewById(R.id.testButton);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-LightItalic.ttf");
//                SnackbarManager.show(
//                        SnackBar.with(MainActivity.this)
//                                .text("Something has been done")
//                                .actionLabel("Undo")
//                                .margin(15, 15)
//                                .backgroundDrawable(R.drawable.custom_shape)
//                                .actionLabelTypeface(tf)
//                                .actionListener(new ActionClickListener() {
//                                    @Override
//                                    public void onActionClicked(SnackBar snackBar) {
//                                        Toast.makeText(MainActivity.this,
//                                                "Action undone",
//                                                Toast.LENGTH_SHORT).show();
//                                    }
//                                }));
//            }
//        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            // do something for lower version
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && isSearchOn && !isSearchAnimationRunning) {
                    isSearchOn = false;
                    isSearchAnimationRunning = true;
                    YoYo.with(Techniques.ZoomOutPosition)
                            .duration(500)
                            .interpolate(new AccelerateDecelerateInterpolator())
                            .withListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    fabSearchWallpaper.setVisibility(View.GONE);
                                    isSearchAnimationRunning = false;
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                    Toast.makeText(MainActivity.this, "canceled", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            })
                            .playOn(findViewById(R.id.fab_search_wallpaper));
                } else if (dy < 0 && !isSearchOn && !isSearchAnimationRunning) {
                    isSearchOn = true;
                    isSearchAnimationRunning = true;
                    fabSearchWallpaper.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.ZoomInPosition)
                            .duration(500)
                            .interpolate(new AccelerateDecelerateInterpolator())
                            .withListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    isSearchAnimationRunning = false;
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                    Toast.makeText(MainActivity.this, "canceled", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            })
                            .playOn(findViewById(R.id.fab_search_wallpaper));
                }
            }
        });

    }


    @SuppressLint("NewApi")
    public void setNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) getSystemService(ns);


        @SuppressWarnings("deprecation")
        Notification notification = new Notification(R.drawable.ic_launcher, "Ticker Text", System.currentTimeMillis());

        RemoteViews notificationView = new RemoteViews(getPackageName(), R.layout.romantic_wallpaper);

        //the intent that is started when the notification is clicked (works)
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notification.contentView = notificationView;
        notification.contentIntent = pendingNotificationIntent;
//        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags = Notification.FLAG_LOCAL_ONLY;

        //this is the intent that is supposed to be called when the button is clicked
        Intent switchIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(this, 0, switchIntent, 0);
//
        notificationView.setOnClickPendingIntent(R.id.download_notification, pendingSwitchIntent);
        notificationManager.notify(1, notification);
    }

    public void setCategorySutter(boolean isOpen) {
        categoryLayout = (LinearLayout) findViewById(R.id.categoryLayout);
        if (isOpen) {
            categoryLayout.setVisibility(View.VISIBLE);
            isSutterOn = true;
        } else {
            categoryLayout.setVisibility(View.GONE);
            isSutterOn = false;
        }
    }

    public void categorySutterChange(int miliseconds) {
        if (isSutterOn) {
            categoryLayout.setVisibility(View.GONE);
            isSutterOn = false;
        } else {
            categoryLayout.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.AlphaAnim)
                    .duration(miliseconds)
                    .interpolate(new AccelerateDecelerateInterpolator())
                    .withListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {

                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            Toast.makeText(MainActivity.this, "canceled", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    })
                    .playOn(findViewById(R.id.categoryLayout));
            isSutterOn = true;
        }
    }

    public void openUri(String uri) {
        try {
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(myIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application can handle this request."
                    + " Please install necessary application", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
//        setRecyclerAdapter(recyclerView, 0);
//        currentWallpaperCategory = WallpaperCategory.ALL;
//        recyclerView.scheduleLayoutAnimation();
//        drawerLayout.openDrawer(GravityCompat.START);

        if (AppStatus.isAppInitiated) {
            setRecyclerAdapter(recyclerView, 0);
            currentWallpaperCategory = WallpaperCategory.ALL;
            drawerLayout.openDrawer(GravityCompat.START);
            YoYo.with(Techniques.AlphaAnim)
                    .duration(1000)
                    .playOn(findViewById(R.id.navigation_view));
            AppStatus.isAppInitiated = false;
        }
    }

    private void initSearchView() {
        searchView = (LinearLayout) findViewById(R.id.search_view);
        searchView.setVisibility(View.GONE);
        isSearchScreenOpen = false;
//        final EditText temp = (EditText) findViewById(R.id.searchKey);
//        findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String query = temp.getText();
//            }
//        });
    }

    private void initRecyclerView(int mColumn) {
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setBackgroundColor(Color.WHITE);
        recyclerView.setLayoutManager(new GridLayoutManager(this, mColumn));
//        recyclerView.addItemDecoration(new SpacesItemDecoration(2));
    }

    private void setRecyclerAdapter(List<ViewModel> mViewModels, String mTitle) {
        setTitle(mTitle);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mViewModels, this);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.scheduleLayoutAnimation();
        recyclerView.setBackgroundColor(Color.WHITE);
        mAdView.setVisibility(View.VISIBLE);
    }

    private void setRecyclerAdapter(RecyclerView mRecyclerView, int menuItemCategoryItem) {

        drawerLayout.closeDrawers();
        possibleOpenReminder();
        initWallpaperLists();
        recyclerView.setVisibility(View.VISIBLE);
        searchView.setVisibility(View.GONE);
        isSearchScreenOpen = false;

        if (menuItemCategoryItem == 0) {
            setRecyclerAdapter(allViewModel, "MOOD WALLPAPER");
        } else if (menuItemCategoryItem == 1) {
            setRecyclerAdapter(happyViewModel, "HAPPY");
        } else if (menuItemCategoryItem == 2) {
            setRecyclerAdapter(sadViewModel, "SAD");
        } else if (menuItemCategoryItem == 3) {
            setRecyclerAdapter(surpriseViewModel, "SURPRISE");
        } else if (menuItemCategoryItem == 4) {
            setRecyclerAdapter(angryViewModel, "ANGRY");
        } else if (menuItemCategoryItem == 5) {
            setRecyclerAdapter(funnyViewModel, "FUNNY");
        } else if (menuItemCategoryItem == 6) {
            setRecyclerAdapter(amazedViewModel, "AMAZED");
        } else if (menuItemCategoryItem == 7) {
            setRecyclerAdapter(dailyViewModel.subList(dailyViewModel.size() - 32, dailyViewModel.size()), "DAILY");
        } else if (menuItemCategoryItem == 8) {
            setRecyclerAdapter(featuredViewModel, "FEATURED");
        } else if (menuItemCategoryItem == 9) {
            setRecyclerAdapter(favouriteViewModel, "FAVOURITE");
        } else if (menuItemCategoryItem == 10) {
            setRecyclerAdapter(searchViewModel, "QUERY : " + searchKey.getText());
        }
    }

    @SuppressWarnings("NewApi")
    private void setupEnterAnimations() {
        Transition transition = TransitionInflater.from(this).inflateTransition(R.transition.changebounds_with_arcmotion);
        getWindow().setSharedElementEnterTransition(transition);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                // Removing listener here is very important because shared element transition is executed again backwards on exit. If we don't remove the listener this code will be triggered again.
                transition.removeListener(this);
                animateButtonsIn();
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });
    }

    @SuppressWarnings("NewApi")
    private void animateRevealHide(final View viewRoot) {
        int cx = (viewRoot.getLeft() + viewRoot.getRight()) / 2;
        int cy = (viewRoot.getTop() + viewRoot.getBottom()) / 2;
        int initialRadius = viewRoot.getWidth();

        android.animation.Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, cx, cy, initialRadius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                super.onAnimationEnd(animation);
                viewRoot.setVisibility(View.INVISIBLE);
            }
        });
        anim.setDuration(getResources().getInteger(R.integer.anim_duration_medium));
        anim.start();
    }

    @SuppressWarnings("NewApi")
    private void setupExitAnimations() {
        Fade returnTransition = new Fade();
        getWindow().setReturnTransition(returnTransition);
        returnTransition.setDuration(getResources().getInteger(R.integer.anim_duration_medium));
        returnTransition.setStartDelay(getResources().getInteger(R.integer.anim_duration_medium));
        returnTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                transition.removeListener(this);
                animateButtonsOut();
                animateRevealHide(recyclerView);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
            }

            @Override
            public void onTransitionCancel(Transition transition) {
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }
        });
    }

    @SuppressWarnings("NewApi")
    private void setupWindowAnimations() {
        interpolator = AnimationUtils.loadInterpolator(this, android.R.interpolator.linear_out_slow_in);
        setupEnterAnimations();
        setupExitAnimations();
    }

    private void animateButtonsIn() {
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            child.animate()
                    .setStartDelay(100 + i * DELAY)
                    .setInterpolator(interpolator)
                    .alpha(1)
                    .scaleX(1)
                    .scaleY(1);
        }
    }

    private void animateButtonsOut() {
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            child.animate()
                    .setStartDelay(i)
                    .setInterpolator(interpolator)
                    .alpha(0)
                    .scaleX(0f)
                    .scaleY(0f);
        }
    }

    @SuppressWarnings("NewApi")
    private android.animation.Animator animateRevealColorFromCoordinates(ViewGroup viewRoot, @ColorRes int color, int x, int y) {
        float finalRadius = (float) Math.hypot(viewRoot.getWidth(), viewRoot.getHeight());

        android.animation.Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, x, y, 0, finalRadius);
        viewRoot.setBackgroundColor(ContextCompat.getColor(this, color));
        anim.setDuration(getResources().getInteger(R.integer.anim_duration_long));
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
        return anim;
    }

    private void searchScreenRevealAnimation() {
        animateButtonsOut();
        android.animation.Animator anim = animateRevealColorFromCoordinates(recyclerView, R.color.sample_yellow,
                (fabSearchWallpaper.getLeft() + fabSearchWallpaper.getRight()) / 2, (fabSearchWallpaper.getTop() + fabSearchWallpaper.getBottom()) / 2
                        + 2 * (fabSearchWallpaper.getTop() - fabSearchWallpaper.getBottom()));

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                animateButtonsIn();
                if (isSearchScreenOpen) {
                    recyclerView.setVisibility(View.VISIBLE);
                    mAdView.setVisibility(View.VISIBLE);
                    searchView.setVisibility(View.GONE);
                    fabSearchWallpaper.setVisibility(View.VISIBLE);
                    isSearchScreenOpen = false;
                } else {
                    recyclerView.setVisibility(View.GONE);
                    searchView.setVisibility(View.VISIBLE);
                    mAdView.setVisibility(View.GONE);
                    searchKey.setEnabled(true);
                    searchKey.clearFocus();
                    searchKey.setHint("Search here ...");
                    fabSearchWallpaper.setVisibility(View.GONE);
                    isSearchScreenOpen = true;
                    isSearchingRunning = false;
                }
            }
        });
    }


    private void initFab() {
        fabSearchWallpaper = (FloatingActionButton) findViewById(R.id.fab_search_wallpaper);
        fabSearchWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchScreenRevealAnimation();
//                if (isSearchScreenOpen) {
//                    recyclerView.setVisibility(View.VISIBLE);
//                    searchView.setVisibility(View.GONE);
//                    isSearchScreenOpen = false;
//                } else {
//                    recyclerView.setVisibility(View.GONE);
//                    searchView.setVisibility(View.VISIBLE);
//                    isSearchScreenOpen = true;
//                }
            }
        });
        fabSearchWallpaper.setVisibility(View.GONE);
    }

    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.drawer_icon);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupDrawerLayout() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    }


    public void possibleOpenReminder() {
        Log.e("REMINDER", reminderCount + "");
        reminderCount++;
        if (reminderCount > 5) {
            reminderCount = 0;
            reminderDialog();
        }
    }

    public void reminderDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = this.getLayoutInflater().inflate(R.layout.alert_label_editor, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        ImageView rate_app_dialog_button = (ImageView) dialogView.findViewById(R.id.rate_app_dialog_button);
        ImageView no_thanks_dialog_button = (ImageView) dialogView.findViewById(R.id.no_thanks_dialog_button);
        ImageView edidremind_dialog_button = (ImageView) dialogView.findViewById(R.id.remind_dialog_button);

        rate_app_dialog_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUri(PLAY_STORE_URL);
            }
        });
        no_thanks_dialog_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        edidremind_dialog_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void helpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.info_dialog, null));
        AlertDialog ad = builder.create();
        ad.setTitle("Mood Wallpaper");
        ad.setButton(AlertDialog.BUTTON_NEGATIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        ad.show();

        TextView noteView = (TextView) ad.findViewById(R.id.web_link);
        noteView.setText("www.somthing.com/zert");
        Linkify.addLinks(noteView, Linkify.ALL);

        TextView email = (TextView) ad.findViewById(R.id.email_zert);
        email.setText("zert@gmail.com");
        Linkify.addLinks(email, Linkify.EMAIL_ADDRESSES);

    }

//    public static void trimCache(Context context) {
//        try {
//            File dir = context.getCacheDir();
//            if (dir != null && dir.isDirectory()) {
//                deleteDir(dir);
//            }
//        } catch (Exception e) {
//            Log.e("TRIM CACHE", e.getMessage());
//        }
//    }
//
//    public static boolean deleteDir(File dir) {
//        if (dir != null && dir.isDirectory()) {
//            String[] children = dir.list();
//            for (int i = 0; i < children.length; i++) {
//                boolean success = deleteDir(new File(dir, children[i]));
//                if (!success) {
//                    return false;
//                }
//            }
//        }
//        // The directory is now empty so delete it
//        return dir.delete();
//    }

    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public void appExitConfirmDialog() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.exit)
                .setTitle("Exit")
                .setMessage("Do you want to exit now ?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                    }
                })
                .show();
    }

    public void clearAppCacheConfirmDialog() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.cache_clear)
                .setTitle("Cache Clear")
                .setMessage("Sure ???")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearApplicationData();
                        sharedPreferenceFavourite.storeFavorites(context, favouriteViewModel);
                        sharedPreferenceDaily.storeDaily(context, dailyViewModel);
                        sharedPreferenceDate.storeDate(context, date);
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                    }
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                YoYo.with(Techniques.AlphaAnim)
                        .duration(1000)
                        .playOn(findViewById(R.id.navigation_view));
                break;
            case R.id.drawer_open:
                setCategorySutter(true);
                drawerLayout.openDrawer(GravityCompat.START);
                YoYo.with(Techniques.AlphaAnim)
                        .duration(1000)
                        .playOn(findViewById(R.id.navigation_view));
                break;
            case R.id.recomment_app:
                //
                break;
            case R.id.rate_app:
                openUri(PLAY_STORE_URL);
                break;
            case R.id.other_apps:
                //
                break;
            case R.id.copy_right:
                //
                break;
            case R.id.clearCache:
                clearAppCacheConfirmDialog();
                break;
            case R.id.help:
                helpDialog();
                break;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onItemClick(View view, ViewModel viewModel) {
        DetailActivity.navigate(this, view.findViewById(R.id.image), viewModel);
    }


    @Override
    public void onItemTextClick(View view, ViewTextModel mViewTextModel) {
        if (!isSearchingRunning) {
            TextView textView = (TextView) view.findViewById(R.id.grid_item_label);
            searchKey.setText(textView.getText().toString());
            searchActionMethod();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isSearchScreenOpen) {
            recyclerView.setBackgroundColor(Color.WHITE);
            recyclerView.setVisibility(View.VISIBLE);
            mAdView.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            isSearchScreenOpen = false;
            isSearchingRunning = false;
        } else {
            appExitConfirmDialog();
        }

    }

    @Override
    public void finish() {
        super.finish();
        Log.e(LOG_TAG, "finish()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(LOG_TAG, "onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(LOG_TAG, "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        Log.e(LOG_TAG, "onResume()");
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.e(LOG_TAG, "onStop()");
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
        Log.e(LOG_TAG, "onPause()");
        overridePendingTransition(R.anim.exit_slide_in, R.anim.exit_slide_out);
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
        AppStatus.isAppInitiated = true;
        Log.e(LOG_TAG, "onDestroy()");
    }


    public class TimerTasks {

        private Timer timer;
        private TimerTask timerTask;
        private Context mContext;

        final Handler handler = new Handler();

        public TimerTasks(Context mContext) {
            this.mContext = mContext;
        }

        public void startTimerTask() {
            timer = new Timer();
            initializeTimerTask();
            timer.schedule(timerTask, 2000);
        }

        public void stopTimerTask() {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }

        public void initializeTimerTask() {
            timerTask = new TimerTask() {
                public void run() {
                    handler.post(new Runnable() {
                        public void run() {
                            isSearchOn = true;
                            YoYo.with(Techniques.SlideInUpPosition)
                                    .duration(1000)
                                    .playOn(findViewById(R.id.fab_search_wallpaper));
                            fabSearchWallpaper.setVisibility(View.VISIBLE);
                            stopTimerTask();
                        }
                    });
                }
            };
        }
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) == 0)
                outRect.top = space;
        }
    }
}

