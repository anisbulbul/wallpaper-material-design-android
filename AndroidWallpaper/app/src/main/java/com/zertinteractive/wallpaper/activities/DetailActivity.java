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

package com.zertinteractive.wallpaper.activities;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.zertinteractive.wallpaper.MainActivity;
import com.zertinteractive.wallpaper.R;
import com.zertinteractive.wallpaper.categories.WallpaperCategory;
import com.zertinteractive.wallpaper.library.photoview.PhotoViewAttacher;
import com.zertinteractive.wallpaper.library.snackbar.SnackBar;
import com.zertinteractive.wallpaper.library.snackbar.SnackbarManager;
import com.zertinteractive.wallpaper.viewmodels.ViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class DetailActivity extends AppCompatActivity {


    float imageWide;
    float imageHeight;

    private Bitmap universalitmap;
    private TimerTasks timerTasks;
    private AdView mAdView;

    private PhotoViewAttacher photoViewAttacher;
    private static final String LOG_TAG = "DETAIL LOG CAT";

    public static final String MOOD_WALLPAPER_DIR = "MoodWallpaper";
    public static final String ALL_WALLPAPER_PREFIX = "all_";
    public static final String SAD_WALLPAPER_PREFIX = "sad_";
    public static final String HAPPY_WALLPAPER_PREFIX = "happy_";
    public static final String SURPRISE_WALLPAPER_PREFIX = "surprise_";
    public static final String ANGRY_WALLPAPER_PREFIX = "angry_";
    public static final String FUNNY_WALLPAPER_PREFIX = "funny_";
    public static final String AMAZED_WALLPAPER_PREFIX = "amazed_";
    public static final String DAILY_WALLPAPER_PREFIX = "daily_";
    public static final String FEATURED_WALLPAPER_PREFIX = "featured_";
    public static final String FAVOURITE_WALLPAPER_PREFIX = "favourite_";
    public static final String SEARCH_WALLPAPER_PREFIX = "searach_";

    public static final String TEMP_WALLPAPER_NAME = "mood_wallpaper_index";
    public static final String TEMP_WALLPAPER_DIR = "mood_wall_temp_8757";

    private static final String EXTRA_TITLE = "com.zertinteractive.wallpaper.extraTitle";
    private static final String EXTRA_CATEGORY = "com.zertinteractive.wallpaper.extraCategory";
    private static final String EXTRA_SUPER_CATEGORY = "com.zertinteractive.wallpaper.extraSuperCategory";
    private static final String EXTRA_IMAGE_SMALL = "com.zertinteractive.wallpaper.extraImageSmall";
    private static final String EXTRA_IMAGE_BIG = "com.zertinteractive.wallpaper.extraImageBig";
    private static final String EXTRA_IMAGE_DOWNLOAD = "com.zertinteractive.wallpaper.extraImageDownload";

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView imageView;
    private ProgressBar progressBar;

    private long enqueue;
    private DownloadManager dm;
    private Toolbar toolbar;
    private static ViewModel mViewModel;
    private Menu detailsMenu;
    private int superCategory;
    private FloatingActionButton floatingActionButton;

    private static Context context;
    private static long imageId = -1;

    public static void navigate(AppCompatActivity activity, View transitionImage, ViewModel viewModel) {
        mViewModel = viewModel;
        Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtra(EXTRA_TITLE, viewModel.getTite());
        intent.putExtra(EXTRA_CATEGORY, viewModel.getWallpaperCategory());
        intent.putExtra(EXTRA_SUPER_CATEGORY, viewModel.getSuperCategory());
        intent.putExtra(EXTRA_IMAGE_SMALL, viewModel.getImageSmall());
        intent.putExtra(EXTRA_IMAGE_BIG, viewModel.getImageBig());
        intent.putExtra(EXTRA_IMAGE_DOWNLOAD, viewModel.getImageDownload());

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionImage, EXTRA_IMAGE_SMALL);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    public static void setContext(Context mContext) {
        context = mContext;
    }

    public void setAsWallpaper(long mImageId) {
        if (mImageId == -1) {
            setAsWallpaperMore();
        } else {
            Intent intent = new Intent(DetailActivity.this, SetWallpaperActivity.class);
            Uri base_uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri item_uri = ContentUris.withAppendedId(base_uri, mImageId);
            intent.putExtra(Intent.EXTRA_STREAM, item_uri);
            Cursor cur = getContentResolver().query(item_uri, null, null, null, null);
            if (cur.moveToFirst()) {
                int colMimetype = cur.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE);
                intent.setDataAndType(item_uri, cur.getString(colMimetype));
            }
            cur.close();
            startActivity(intent);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransitions();
        setContentView(R.layout.activity_detail);

        String[] projection = {
                MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DISPLAY_NAME
        };
        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[]{
                TEMP_WALLPAPER_DIR
        };

        Cursor mImageCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
        mImageCursor.moveToFirst();
        if (mImageCursor.getCount() == 1) {
            imageId = mImageCursor.getInt((mImageCursor.getColumnIndex(MediaStore.Images.Media._ID)));
            Log.e("CROP", "" + imageId);
            mImageCursor.close();
        }

        checkDir();
        timerTasks = new TimerTasks(context);

        ViewCompat.setTransitionName(findViewById(R.id.app_bar_layout), EXTRA_IMAGE_SMALL);
        supportPostponeEnterTransition();

        superCategory = getIntent().getExtras().getInt(EXTRA_SUPER_CATEGORY);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab_set_as_wallpaper);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAsWallpaper(imageId);
            }
        });

        toolbar = ((Toolbar) findViewById(R.id.toolbar));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setVisibility(View.GONE);

        String itemTitle = getIntent().getStringExtra(EXTRA_TITLE);

        progressBar = (ProgressBar) findViewById(R.id.progressBarImage);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(itemTitle);
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        imageView = (ImageView) findViewById(R.id.image);
        Picasso.with(this)
                .load(getIntent().getStringExtra(EXTRA_IMAGE_SMALL))
                .into(imageView, new Callback() {

                    @SuppressWarnings("NewApi")
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                        float ImageHieghtTemp = bitmap.getHeight();
                        float ImageWidthTemp = bitmap.getWidth();

                        Point size = new Point();
                        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                        wm.getDefaultDisplay().getRealSize(size);
                        float widthMain = size.x;
                        float heightMain = size.y;

                        imageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        int addWidth = imageView.getMeasuredWidth();

//                        Display display = getWindowManager().getDefaultDisplay();
//                        float widthMain = display.getWidth();
//                        float heightMain = display.getHeight();

                        Log.e(LOG_TAG, "IMAGE_MAIN : " + ImageWidthTemp + " : " + ImageHieghtTemp);

                        float imageHeight = widthMain * ((float) (bitmap.getHeight()) / (float) (bitmap.getWidth()));
                        float scaleXY = heightMain / imageHeight;

                        if (scaleXY > 1.0007f) {
                            Log.e(LOG_TAG, "SCALE FACTORE : " + scaleXY + " : " + 1.10454545f * scaleXY);
                            scaleXY = 1.8f * scaleXY;
                        }

                        PhotoViewAttacher mAttacherTest = new PhotoViewAttacher(imageView, true, false);
                        mAttacherTest.setZoomScale(scaleXY);
                        mAttacherTest.setScale(scaleXY, (widthMain) / 2, heightMain / 2, true);

                        Log.e(LOG_TAG, "DETAILS : " + widthMain + " : " + heightMain + " : " + scaleXY + " : " + bitmap.getWidth() + " : " + bitmap.getHeight());


                        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                            public void onGenerated(Palette palette) {
                                applyPalette(palette);
                            }
                        });
                    }

                    @Override
                    public void onError() {

                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
//        clearApplicationData();
        new ImageLoadTask().execute("");
        initDownloadComponents();

        mAdView = (AdView) findViewById(R.id.adViewFullImage);
        mAdView.setVisibility(View.GONE);

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

        context.registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        context.unregisterReceiver(receiver);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        try {
            return super.dispatchTouchEvent(motionEvent);
        } catch (NullPointerException e) {
            return false;
        }
    }

    private void initActivityTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide transition = new Slide();
            transition.excludeTarget(android.R.id.statusBarBackground, true);
            getWindow().setEnterTransition(transition);
            getWindow().setReturnTransition(transition);
        }
    }

    private void applyPalette(Palette palette) {
        int primaryDark = getResources().getColor(R.color.primary_dark);
        int primary = getResources().getColor(R.color.primary);
        collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(primary));
        collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(primaryDark));
        updateBackground(floatingActionButton, palette);
        supportStartPostponedEnterTransition();
    }

    private void updateBackground(FloatingActionButton fab, Palette palette) {
        int lightVibrantColor = palette.getLightVibrantColor(getResources().getColor(R.color.palette_default_color));
        int vibrantColor = palette.getVibrantColor(getResources().getColor(R.color.accent));
        fab.setRippleColor(lightVibrantColor);
        fab.setBackgroundTintList(ColorStateList.valueOf(vibrantColor));
    }

    public class ImageLoadTask extends AsyncTask<String, Integer, Bitmap> {

        Bitmap returnResult;
        boolean isReturnable = false;

        public ImageLoadTask() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isReturnable = false;

        }

        private Bitmap downloadImagePreviewTest() {
            // initilize the default HTTP client object
            final DefaultHttpClient client = new DefaultHttpClient();

            //forming a HttoGet request
            final HttpGet getRequest = new HttpGet(getIntent().getStringExtra(EXTRA_IMAGE_BIG));
            try {

                HttpResponse response = client.execute(getRequest);

                //check 200 OK for success
                final int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != HttpStatus.SC_OK) {
                    Log.w("ImageDownloader", "Error " + statusCode +
                            " while retrieving bitmap from " + getIntent().getStringExtra(EXTRA_IMAGE_BIG));
                    return null;

                }

                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = null;
                    try {
                        // getting contents from the stream
                        inputStream = entity.getContent();

                        // decoding stream data back into image Bitmap that android understands
                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        return bitmap;
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        entity.consumeContent();
                    }
                }
            } catch (Exception e) {
                // You Could provide a more explicit error message for IOException
                getRequest.abort();
                Log.e("ImageDownloader", "Something went wrong while" +
                        " retrieving bitmap from " + getIntent().getStringExtra(EXTRA_IMAGE_BIG) + e.toString());
            }

            return null;
        }

        private Bitmap downloadImagePreview() {

            try {

                Thread.currentThread();
                Thread.sleep(500);


//                final HttpURLConnection connection = (HttpURLConnection) new URL(getIntent()
//                        .getStringExtra(EXTRA_IMAGE_BIG)).openConnection();
//                connection.setDoInput(true);
//                connection.connect();

//                ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance

// Load image, decode it to Bitmap and display Bitmap in ImageView (or any other view
//  which implements ImageAware interface)
//                imageLoader.displayImage(imageUri, imageView);

// Load image, decode it to Bitmap and return Bitmap to callback
//                imageLoader.loadImage(getIntent().getStringExtra(EXTRA_IMAGE_BIG), new SimpleImageLoadingListener() {
//                    @Override
//                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                        // Do whatever you want with Bitmap
//                    }
//                });

                // Load image, decode it to Bitmap and return Bitmap synchronously
//                Log.e("HELP_ME", getIntent().getStringExtra(EXTRA_IMAGE_BIG));
//                return imageLoader.loadImageSync(getIntent().getStringExtra(EXTRA_IMAGE_BIG));


//                imageLoader.loadImage(getIntent().getStringExtra(EXTRA_IMAGE_BIG),
//                        new SimpleImageLoadingListener() {
//                            @Override
//                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                                callMethodImage(loadedImage);
//                            }
//                        });


//                DisplayImageOptions displayOptions;
//                displayOptions = DisplayImageOptions.createSimple();
//
//                ImageSize minImageSize = new ImageSize(70, 70); // 70 - approximate size of ImageView in widget
//
//                ImageLoader.getInstance()
//                        .loadImage(getIntent().getStringExtra(EXTRA_IMAGE_BIG),
//                                minImageSize, displayOptions, new SimpleImageLoadingListener() {
//                                    @Override
//                                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
////                                        views.setImageViewBitmap(R.id.image_right, loadedImage);
////                                        appWidgetManager.updateAppWidget(appWidgetId, views);
//                                        Log.e("HELP_ME", "Load Successfull ... ");
//                                        callMethodImage(loadedImage);
////                                        isReturnable = true;
//                                    }
//
//                                    @Override
//                                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//                                        super.onLoadingFailed(imageUri, view, failReason);
//                                        Log.e("HELP_ME", "Load Failed ... ");
//                                    }
//                                });
//
//                if (isReturnable)
//                    return returnResult;

//                return BitmapFactory.decodeStream(connection.getInputStream());

//                return BitmapFactory.decodeStream(new URL(getIntent().getStringExtra(EXTRA_IMAGE_BIG))
//                        .openConnection().getInputStream());

                URL myfileurl = new URL(getIntent().getStringExtra(EXTRA_IMAGE_BIG));
                HttpURLConnection conn = (HttpURLConnection) myfileurl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                int length = conn.getContentLength();
                int[] bitmapData = new int[length];
                byte[] bitmapData2 = new byte[length];
                InputStream is = conn.getInputStream();
                BitmapFactory.Options options = new BitmapFactory.Options();

                return BitmapFactory.decodeStream(is, null, options);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return downloadImagePreviewTest();
        }

        @SuppressWarnings("NewApi")
        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (result == null) {
                SnackbarManager.show(
                        SnackBar.with(context)
                                .text("Netwoek Peoblems ..."));
                finish();
            } else {

                universalitmap = result;
                imageView.setImageBitmap(result);

                imageWide = result.getWidth();
                imageHeight = result.getHeight();

                File file = new File(Environment.getExternalStorageDirectory().toString(),
                        "/" + TEMP_WALLPAPER_DIR + "/" + TEMP_WALLPAPER_NAME + ".png");

                try {
                    OutputStream fOut = new FileOutputStream(file);
                    result.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    MediaStore.Images.Media.insertImage(getContentResolver(),
                            file.getAbsolutePath(), file.getName(), file.getName());
                    fOut.flush();
                    fOut.close();

                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }

                imageView.setAlpha(1.0f);

                Point size = new Point();
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                wm.getDefaultDisplay().getRealSize(size);
                float widthMain = size.x;
                float heightMain = size.y;

//            Display display = getWindowManager().getDefaultDisplay();
//            float widthMain = display.getWidth();
//            float heightMain = display.getHeight();

                float imageRatio = widthMain * (imageHeight / imageWide);
                float scaleXY = heightMain / imageRatio;

                // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
                photoViewAttacher = new PhotoViewAttacher(imageView, true, true);
                photoViewAttacher.setZoomScale(scaleXY);
                photoViewAttacher.setScale(scaleXY, (widthMain) / 2, 0, true);

                floatingActionButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
            }


        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    public void setMenuItemFavourite() {
        if (superCategory == 3) {
            MenuItem favouriteItem = detailsMenu.findItem(R.id.add_to_favourite);
            favouriteItem.setTitle("Remove Favourite");
        } else {
            MenuItem favouriteItem = detailsMenu.findItem(R.id.add_to_favourite);
            favouriteItem.setTitle("Add to Favourite");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image, menu);
        this.detailsMenu = menu;
        setMenuItemFavourite();
        return true;
    }

    public void shareImage() {

        String path = Environment.getExternalStorageDirectory().toString();
        File file = new File(path, "/" + TEMP_WALLPAPER_DIR + "/" + TEMP_WALLPAPER_NAME + ".png");

        Uri imageUri = Uri.fromFile(file);
        if (file.exists()) {
            ;
            Log.e("FILE - ", file.getAbsolutePath());
        } else {
            Log.e("ERROR - ", file.getAbsolutePath());
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, "Mood Wallpaper");
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.setType("image/*");
        startActivity(intent);
    }

    public void downloadOriginalImage() {
        String path = Environment.getExternalStorageDirectory().toString();
        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Request request = new Request(
                Uri.parse(getIntent().getStringExtra(EXTRA_IMAGE_DOWNLOAD)));
        String fileName = getIntent().getStringExtra(EXTRA_IMAGE_DOWNLOAD);
        fileName = fileName.substring(fileName.length() - 15, fileName.length() - 9); // H_89.jpg
        while (!isDigit(fileName.charAt(0))) {
            fileName = fileName.substring(1);
        }
        File file = new File(path, "/" + MOOD_WALLPAPER_DIR + "/" + getPrefixName() + fileName);
        if (file.exists()) {
            SnackbarManager.show(
                    SnackBar.with(DetailActivity.this)
                            .text("ALLREADY DOWNLOADED: " + getPrefixName() + " : " + fileName));
        } else {
            request.setTitle("Mood Wallpaper");
            request.setDescription(file.getAbsolutePath());
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(MOOD_WALLPAPER_DIR, getPrefixName() + fileName);
            enqueue = dm.enqueue(request);
            SnackbarManager.show(
                    SnackBar.with(DetailActivity.this)
                            .text("DOWNLOADED ... : " + getPrefixName() + " : " + fileName));
        }
    }

    public void setFavourite() {
        String fileName = getIntent().getStringExtra(EXTRA_IMAGE_DOWNLOAD);
        fileName = fileName.substring(fileName.length() - 15, fileName.length() - 9); // H_89.jpg
        while (fileName.length() > 0 && !isDigit(fileName.charAt(0))) {
            fileName = fileName.substring(1);
        }
        while (fileName.length() > 0 && !isDigit(fileName.charAt(fileName.length() - 1))) {
            fileName = fileName.substring(0, fileName.length() - 1);
        }
        int position = Integer.parseInt(fileName) - 1;
        if (superCategory != 3) {
            mViewModel.setSuperCategory(3);
            superCategory = 3;
            MainActivity.resetWallpapers(position, 3, mViewModel);
            timerTasks.startTimerTask();
        } else {
            mViewModel.setSuperCategory(0);
            superCategory = 0;
            MainActivity.resetWallpapers(position, 0, mViewModel);
            timerTasks.startTimerTask();
        }
    }

    public void setAsWallpaperMore() {

        String path = Environment.getExternalStorageDirectory().toString();
        File file = new File(path, "/" + TEMP_WALLPAPER_DIR + "/" + TEMP_WALLPAPER_NAME + ".png");
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(uri, "image/png");
        intent.putExtra("mimeType", "image/png");
        startActivity(Intent.createChooser(intent, "Set as : Mood Wallpaper"));

//        WallpaperManager myWallpaperManager
//                = WallpaperManager.getInstance(getApplicationContext());
//        try {
//            myWallpaperManager.setBitmap(((BitmapDrawable) imageView.getDrawable()).getBitmap());
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

    @SuppressWarnings("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.share_image:
                shareImage();
                return true;
            case R.id.add_to_favourite:
                setFavourite();
                return true;
            case R.id.set_as_wallpaper:
//                setAsWallpaper(imageId);

                WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                try {
                    myWallpaperManager.setBitmap(universalitmap);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                return true;
            case R.id.download_image_original:
                downloadOriginalImage();
                return true;
            case R.id.downloadOriginalImage:
                downloadOriginalImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        toolbar.setVisibility(View.INVISIBLE);
        Log.e("onBackPressed()", "onBackPressed()");
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
        Log.e(LOG_TAG, "onPause()");
        overridePendingTransition(R.anim.details_slide_in, R.anim.details_slide_out);
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
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
        Log.e("onDestroy()", "onDestroy()");
    }

    public static void checkDir() {
        String path = Environment.getExternalStorageDirectory().toString();
        File dir = new File(path + "/" + MOOD_WALLPAPER_DIR + "/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dir2 = new File(path + "/" + TEMP_WALLPAPER_DIR + "/");
        if (!dir2.exists()) {
            dir2.mkdirs();
        }
    }


    public static boolean isDigit(char charA) {
        if (charA - '0' >= 0 && charA - '0' <= 9)
            return true;
        return false;
    }

    public String getPrefixName() {
        if (getIntent().getSerializableExtra(EXTRA_CATEGORY).equals(WallpaperCategory.HAPPY)) {
            return HAPPY_WALLPAPER_PREFIX;
        } else if (getIntent().getSerializableExtra(EXTRA_CATEGORY).equals(WallpaperCategory.SAD)) {
            return SAD_WALLPAPER_PREFIX;
        } else if (getIntent().getSerializableExtra(EXTRA_CATEGORY).equals(WallpaperCategory.SURPRISE)) {
            return SURPRISE_WALLPAPER_PREFIX;
        } else if (getIntent().getSerializableExtra(EXTRA_CATEGORY).equals(WallpaperCategory.ANGRY)) {
            return ANGRY_WALLPAPER_PREFIX;
        } else if (getIntent().getSerializableExtra(EXTRA_CATEGORY).equals(WallpaperCategory.FUNNY)) {
            return FUNNY_WALLPAPER_PREFIX;
        } else if (getIntent().getSerializableExtra(EXTRA_CATEGORY).equals(WallpaperCategory.AMAZED)) {
            return AMAZED_WALLPAPER_PREFIX;
        } else if (getIntent().getSerializableExtra(EXTRA_CATEGORY).equals(WallpaperCategory.DAILY)) {
            return DAILY_WALLPAPER_PREFIX;
        } else if (getIntent().getSerializableExtra(EXTRA_CATEGORY).equals(WallpaperCategory.FEATURED)) {
            return FEATURED_WALLPAPER_PREFIX;
        } else if (getIntent().getSerializableExtra(EXTRA_CATEGORY).equals(WallpaperCategory.FAVOURITE)) {
            return FAVOURITE_WALLPAPER_PREFIX;
        } else if (getIntent().getSerializableExtra(EXTRA_CATEGORY).equals(WallpaperCategory.SEARCH)) {
            return SEARCH_WALLPAPER_PREFIX;
        }
        return ALL_WALLPAPER_PREFIX;

    }

    private void copyFile(String inputPath, String outputPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

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
            timer.schedule(timerTask, 200);
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
                            setMenuItemFavourite();
                            stopTimerTask();
                        }
                    });
                }
            };
        }
    }
}
