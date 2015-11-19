package com.zertinteractive.wallpaper.activities;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.zertinteractive.wallpaper.R;
import com.zertinteractive.wallpaper.library.snackbar.SnackBar;
import com.zertinteractive.wallpaper.library.snackbar.SnackbarManager;
import com.zertinteractive.wallpaper.library.util.LogCategory;
import com.zertinteractive.wallpaper.library.util.WorkerBase;
import com.zertinteractive.wallpaper.setwallpaper.MyApp;

import java.io.IOException;
import java.io.InputStream;

public final class SetWallpaperActivity extends AppCompatActivity {
    private static LogCategory log = new LogCategory("SET_WALLPAPER_LOG");


    //adView
    private AdView mAdView;

    //original image attributes
    private float originalImageWidth;
    private float originalImageHeight;
    private Canvas cFrame, c;
    // UI parts
    private FrameLayout flOuter;
    private ImageView ivImage;
    private ImageView ivSelection;
    //    ImageView ivSelection2;
    private ImageView standardLinearLayout;
    private ImageView fixedLinearLayout;
    private ImageView entireLinearLayout;
    private CheckBox checkBoxScrollable;
    private Button btnOk;
    private Button btnCancel;
    private boolean isScrollable;
    private boolean isWallpaperSet;

    private Handler ui_handler;
    private Paint paint;

    // Output size of wallpaper
    private float wall_image_aspect;
    private int wall_real_w;
    private int wall_real_h;
    private int wall_w;
    private int wall_h;
    private final static int standardRatio = 2;

    // Input image
    private Bitmap src_image;
    private Bitmap wall_image;


    private static final int IMAGE_DIMENSION_HORIZONTAL = 1;
    private static final int IMAGE_DIMENSION_VERTICAL = 2;
    private static final int IMAGE_DIMENSION_NOT_STANDARD = 3;
    private int imageDimensionCategory = IMAGE_DIMENSION_VERTICAL;
    // Position of the display image (display frame criteria)
    private Bitmap shown_image;
    private RectF shown_image_rect;
    private Rect src_rect;

    // Movement at the start of the selection
    private Rect prev_selection = new Rect();

    // Move mode
    private int tracking_mode = 0;
    private static final int TRACK_NONE = 0;
    private static final int TRACK_ZOOM = 1;
    private static final int TRACK_MOVE = 2;

    // Width of the enlargement operation grip around selection
    private float border_grip;

    // Rectangle in the center at the start of the zoom (raw coordinates)
    private float zoom_center_x;
    private float zoom_center_y;

    // Distance of the center and the touch position at the start of the zoom
    private float zoom_start_len;

    // Touch position at the start of movement (raw coordinates)
    private float touch_start_x;
    private float touch_start_y;

    //
    private ProgressDialog dialog;
    private WallpaperManager wpm;
    private Uri uri;
    private boolean bLoading;
    private DisplayMetrics metrics;
    private boolean bEntire;
    private boolean bStandard;
    private boolean bFixed = true;
    private int opt_output_width;
    private int opt_output_height;
    private int opt_display_width;
    private int opt_display_height;

    private Context mContext;

    private void init_resource() {

        setContentView(R.layout.activity_set_as_wallpaper);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mContext = this;
        isWallpaperSet = false;

        flOuter = (FrameLayout) findViewById(R.id.flOuter);
        ivImage = (ImageView) findViewById(R.id.ivImage);
        ivSelection = (ImageView) findViewById(R.id.ivSelection);
//        ivSelection2 = (ImageView) findViewById(R.id.ivSelection2);
        btnOk = (Button) findViewById(R.id.btnSetWallPaper);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        standardLinearLayout = (ImageView) findViewById(R.id.standard);
        fixedLinearLayout = (ImageView) findViewById(R.id.fixed);
        entireLinearLayout = (ImageView) findViewById(R.id.entire);
        fixedLinearLayout.setBackgroundResource(R.drawable.circle_blue);
        checkBoxScrollable = (CheckBox) findViewById(R.id.checkBoxScrollable);
        checkBoxScrollable.setEnabled(false);
        isScrollable = false;
        // Calculate grip width
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        border_grip = metrics.density * 20;

        opt_display_width = getResources().getDisplayMetrics().widthPixels;
        opt_display_height = getResources().getDisplayMetrics().heightPixels;

        //
        ui_handler = new Handler();

        //
        wpm = WallpaperManager.getInstance(this);

        btnOk.setEnabled(false);

        // Expansion and movement of the selected range
        ivSelection.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // It does not process if yet initialized
                if (bLoading) return false;
                // It is not processed if the entire mode
                if (bEntire) return false;

                float x = event.getX();
                float y = event.getY();
                float raw_x = event.getRawX();
                float raw_y = event.getRawY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // The end of the drag operation
                        if (tracking_mode != TRACK_NONE) {
                            tracking_mode = TRACK_NONE;
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_DOWN: //Start of move expansion
                        if (tracking_mode == TRACK_NONE) {
                            // Remember the position and width of the selection at the time of start of movement
                            LinearLayout.LayoutParams lpSelection = (LinearLayout.LayoutParams) ivSelection.getLayoutParams();
                            prev_selection.left = lpSelection.leftMargin;
                            prev_selection.top = lpSelection.topMargin;
                            prev_selection.right = prev_selection.left + ivSelection.getWidth();
                            prev_selection.bottom = prev_selection.top + ivSelection.getHeight();

                            if (x < border_grip || prev_selection.width() - x < border_grip
                                    || y < border_grip || prev_selection.height() - y < border_grip
                                    ) {
                                // Scale to grab the Hajikko
                                tracking_mode = TRACK_ZOOM;
                                // Center position at the touch start (raw coordinates)
                                zoom_center_x = raw_x - x + ivSelection.getWidth() / 2;
                                zoom_center_y = raw_y - y + ivSelection.getHeight() / 2;
                                // Distance of the center and the current position
                                zoom_start_len = (float) Math.sqrt(
                                        Math.pow(raw_x - zoom_center_x, 2)
                                                + Math.pow(raw_y - zoom_center_y, 2)
                                );
                            } else {
                                // Move and grab the center
                                tracking_mode = TRACK_MOVE;
                                // Touch position at the touch start
                                touch_start_x = raw_x;
                                touch_start_y = raw_y;
                            }
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (tracking_mode == TRACK_ZOOM && !bStandard && !bFixed) {
                            // Examine the distance from the center
                            float len = (float) Math.sqrt(
                                    Math.pow(raw_x - zoom_center_x, 2)
                                            + Math.pow(raw_y - zoom_center_y, 2)
                            );
                            if (len < border_grip * 2) len = border_grip * 2;

                            // Changes size according to the distance change of the
                            int new_w, new_h;
                            if (wall_image_aspect >= 1) {
                                new_w = (int) (0.5 + prev_selection.width() * len / zoom_start_len);
                                new_h = (int) (0.5 + new_w / wall_image_aspect);
                            } else {
                                new_h = (int) (0.5 + prev_selection.height() * len / zoom_start_len);
                                new_w = (int) (0.5 + new_h * wall_image_aspect);
                            }
                            // クリッピング
                            if (new_w > shown_image_rect.width()) {
                                new_w = (int) shown_image_rect.width();
                                new_h = (int) (0.5 + new_w / wall_image_aspect);
                            }
                            if (new_h > shown_image_rect.height()) {
                                new_h = (int) shown_image_rect.height();
                                new_w = (int) (0.5 + new_h * wall_image_aspect);
                            }
                            setSelection(
                                    (prev_selection.left + prev_selection.right) / 2 - new_w / 2
                                    , (prev_selection.top + prev_selection.bottom) / 2 - new_h / 2
                                    , new_w
                                    , new_h
                            );

                            return true;
                        }
                        if (tracking_mode == TRACK_MOVE) {
                            // And update the position of the mobile mode
                            setSelection(
                                    prev_selection.left + (int) (0.5 + raw_x - touch_start_x)
                                    , prev_selection.top + (int) (0.5 + raw_y - touch_start_y)
                                    , prev_selection.width()
                                    , prev_selection.height()
                            );
                            return true;
                        }
                        break;
                }
                return false;
            }
        });

        // キャンセルボタン
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 壁紙セット
        btnOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("OK", "1");
                if (bLoading) return;
                Log.e("OK", "2");
                // 既に作業中なら何もしない
                if (dialog != null) return;
                Log.e("OK", "3");
                if (wp_task != null) return;
                Log.e("OK", "4");
                // 処理中ダイアログを表示
                dialog = ProgressDialog.show(
                        SetWallpaperActivity.this
                        , getText(R.string.wallpaper_progress_title)
                        , getText(R.string.wallpaper_progress_message)
                        , true);
                // Background processing
                Log.e("OK", "5");
                wp_task = new WallpaperTask();
                wp_task.start();
            }
        });
        standardLinearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                standardWallpaperOption();
            }
        });
        fixedLinearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fixedWallpaperOption();
            }
        });
        entireLinearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                entireWallpaperOption();
            }
        });
        checkBoxScrollable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isScrollable = isChecked;
            }
        });
    }

    private void entireWallpaperOption() {
        if (!bEntire) {
            checkBoxScrollable.setEnabled(true);
            standardLinearLayout.setBackgroundResource(R.drawable.transparent_square);
            fixedLinearLayout.setBackgroundResource(R.drawable.transparent_square);
            entireLinearLayout.setBackgroundResource(R.drawable.circle_blue);
            bFixed = false;
            bStandard = false;
            bEntire = true;
            setSelection(0, 0, 0, 0);
            Log.e("CONF", "ENTIRE");
        }
    }

    private void standardWallpaperOption() {
        if (!bStandard) {

            if (bEntire) {
                fixedWallpaperOption();
            }
            checkBoxScrollable.setEnabled(false);
            checkBoxScrollable.setChecked(true);
            standardLinearLayout.setBackgroundResource(R.drawable.circle_blue);
            fixedLinearLayout.setBackgroundResource(R.drawable.transparent_square);
            entireLinearLayout.setBackgroundResource(R.drawable.transparent_square);


            if (imageDimensionCategory == IMAGE_DIMENSION_HORIZONTAL) {
                int tempWidth = prev_selection.width();
                if (!bEntire && tempWidth < prev_selection.height()) {
                    tempWidth *= standardRatio;
                }
                setSelection(
                        (int) (shown_image_rect.left + shown_image_rect.right - tempWidth) / 2
                        , prev_selection.top
                        , tempWidth
                        , prev_selection.height()
                );
            } else if (imageDimensionCategory == IMAGE_DIMENSION_VERTICAL) {
                int tempHeight = prev_selection.height();
                if (!bEntire && tempHeight > prev_selection.width()) {
                    tempHeight /= standardRatio;
                }
                setSelection(
                        prev_selection.left
                        , (int) (shown_image_rect.top + shown_image_rect.bottom - tempHeight) / 2
                        , prev_selection.width()
                        , tempHeight
                );
            } else if (imageDimensionCategory == IMAGE_DIMENSION_NOT_STANDARD) {
                int tempHeight = prev_selection.height();
                if (!bEntire && tempHeight > prev_selection.width()) {
                    tempHeight /= standardRatio;
                }

                float left = prev_selection.left;
                float top = prev_selection.top;
                float right = prev_selection.right;
                float bottom = prev_selection.bottom;
                float widthTemp = prev_selection.width();
                float heightTemp = prev_selection.height();


                if (!bEntire && heightTemp > prev_selection.width()) {
                    heightTemp /= standardRatio;
                }

                float ratioTemp = (shown_image_rect.right - shown_image_rect.left) / widthTemp;
                widthTemp *= ratioTemp;
                heightTemp *= ratioTemp;

                setSelection(
                        (int) left
                        , (int) (shown_image_rect.top + shown_image_rect.bottom - heightTemp) / 2
                        , (int) widthTemp
                        , (int) heightTemp
                );
            }
            bFixed = false;
            bStandard = true;
            bEntire = false;
        }
    }

    private void fixedWallpaperOption() {
        if (!bFixed) {
            checkBoxScrollable.setEnabled(false);
            standardLinearLayout.setBackgroundResource(R.drawable.transparent_square);
            fixedLinearLayout.setBackgroundResource(R.drawable.circle_blue);
            entireLinearLayout.setBackgroundResource(R.drawable.transparent_square);
            bFixed = true;
            bEntire = false;
            bStandard = false;
            Log.e("CONF", "FIXED");
            if (imageDimensionCategory == IMAGE_DIMENSION_HORIZONTAL) {
                int tempWidth = prev_selection.width();
                if (tempWidth > prev_selection.height()) {
                    tempWidth /= standardRatio;
                }
                setSelection(
                        (int) (shown_image_rect.left + shown_image_rect.right - tempWidth) / 2
                        , prev_selection.top
                        , tempWidth
                        , prev_selection.height()
                );
            } else if (imageDimensionCategory == IMAGE_DIMENSION_VERTICAL) {
                int tempHeight = prev_selection.height();
                if (tempHeight < prev_selection.width()) {
                    tempHeight *= standardRatio;
                }
                setSelection(
                        prev_selection.left
                        , prev_selection.top
                        , prev_selection.width()
                        , tempHeight
                );
            } else if (imageDimensionCategory == IMAGE_DIMENSION_NOT_STANDARD) {


                float left = prev_selection.left;
                float top = prev_selection.top;
                float right = prev_selection.right;
                float bottom = prev_selection.bottom;
                float widthTemp = prev_selection.width();
                float heightTemp = prev_selection.height();


//                int tempHeight = prev_selection.height();
                if (heightTemp < prev_selection.width()) {
                    heightTemp *= standardRatio;
                }

                float ratioTemp = heightTemp / (shown_image_rect.bottom - shown_image_rect.top);
                widthTemp /= ratioTemp;
                heightTemp /= ratioTemp;
                setSelection(
                        (int) (shown_image_rect.left + shown_image_rect.right - widthTemp) / 2
                        , prev_selection.top
                        , (int) widthTemp
                        , (int) heightTemp
                );
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        log.d("onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    // Interpretation of the page configuration parameters
    private void init_page(Intent intent) {
        bLoading = true;
        src_image = null;
        uri = intent.getData();
        if (uri == null) {
            log.d("intent=%s uri=%s type=%s extra=%s"
                    , intent
                    , intent.getData()
                    , intent.getType()
                    , intent.getExtras()
            );
            Bundle extra = intent.getExtras();
            if (extra != null) {
                for (String key : extra.keySet()) {
                    log.d("key=%s", key);
                }
                uri = (Uri) extra.get(Intent.EXTRA_STREAM);
            }
            if (uri == null) {
                finish();
                return;
            }
        }
        log.d("uri=%s", uri);
    }

    // Image load task
    ImageLoaderWorker loader_worker;

    class ImageLoaderWorker extends WorkerBase {
        volatile boolean bCancelled = false;

        @Override
        public void cancel() {
            bCancelled = true;
            notifyEx();
        }

        @SuppressWarnings("NewApi")
        @Override
        public void run() {
            if (src_image == null) {
                if (uri == null) {
                    ui_handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isFinishing()) return;
                            Toast.makeText(SetWallpaperActivity.this, "missing uri in arguments.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                log.d("loading image..");

                // Options for image size check
                BitmapFactory.Options check_option = new BitmapFactory.Options();
                check_option.inJustDecodeBounds = true;
                check_option.inDensity = 0;
                check_option.inTargetDensity = 0;
                check_option.inDensity = 0;
                check_option.inScaled = false;

                // Option for image load
                BitmapFactory.Options load_option = new BitmapFactory.Options();
                load_option.inPurgeable = true;
                load_option.inDensity = 0;
                load_option.inTargetDensity = 0;
                load_option.inDensity = 0;
                load_option.inScaled = false;

                // Color at the time of load depth
                int pixel_bytes;
                check_option.inPreferredConfig = Bitmap.Config.ARGB_8888;
                load_option.inPreferredConfig = Bitmap.Config.ARGB_8888;
                pixel_bytes = 4;

                ContentResolver cr = getContentResolver();
                InputStream is;

                // Examine the image size
                try {
                    is = cr.openInputStream(uri);
                    try {
                        check_option.outHeight = 0;
                        check_option.outWidth = 0;
                        BitmapFactory.decodeStream(is, null, check_option);
                    } finally {
                        is.close();
                    }
                } catch (Throwable ex) {
                    // IOException
                    // SecurityException: reading com.android.providers.telephony.MmsProvider
                    ex.printStackTrace();
                }
                if (check_option.outWidth < 1 || check_option.outHeight < 1) {
                    log.e("load failed.");
                    ui_handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isFinishing()) return;
                            Toast.makeText(SetWallpaperActivity.this, "load failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                // To change the sample size, if necessary by examining the amount of data
                int data_size = check_option.outWidth * check_option.outHeight * pixel_bytes; // 面積と色深度
                int limit_size = 1024 * 1024 * 10;
                int samplesize = 1;
                while (data_size / (float) (samplesize * samplesize) >= limit_size) {
                    samplesize++;
                }
                load_option.inSampleSize = samplesize;

                // load bitmap
                try {
                    is = cr.openInputStream(uri);
                    try {
                        src_image = BitmapFactory.decodeStream(is, null, load_option);
                    } finally {
                        is.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (src_image == null) {
                    log.e("load failed.");
                    ui_handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isFinishing()) return;
                            Toast.makeText(SetWallpaperActivity.this, "load failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                int row_bytes = src_image.getRowBytes();
                int pixel_bytes2 = row_bytes / src_image.getWidth();

                originalImageWidth = src_image.getWidth();
                originalImageHeight = src_image.getHeight();

                Point size = new Point();
                WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                wm.getDefaultDisplay().getRealSize(size);

                float ratioOriginal = originalImageWidth / originalImageHeight;
                float ratioScreen = (float) (size.x) / (float) (size.y);

                imageDimensionCategory = originalImageWidth > originalImageHeight ? IMAGE_DIMENSION_HORIZONTAL : IMAGE_DIMENSION_VERTICAL;
                if (ratioOriginal > ratioScreen && ratioOriginal < 2 * ratioScreen) {
                    imageDimensionCategory = IMAGE_DIMENSION_NOT_STANDARD;
                }
                Log.e("DIMENSION", imageDimensionCategory + "");


                float temp = size.y;
                wall_real_w = opt_display_width;
                float scaleTemp = (temp / originalImageHeight);
                wall_real_h = (int) temp;
                opt_display_width = size.x;
                opt_display_height = size.y;
//                Log.e("COST", "" + temp + " " + scaleTemp + " " + wall_real_w + " " + wall_real_h);
                Log.e("COST", temp + " ORIGINAL " + originalImageWidth + " " + originalImageHeight + " REAL "
                        + wall_real_w + " " + wall_real_h + " SCREEN " + opt_display_width + " " + opt_display_height);

                log.d("wallpaper=%dx%d, display=%dx%d, overriding %dx%d"
                        , wall_real_w
                        , wall_real_h
                        , opt_display_width
                        , opt_display_height
                        , opt_output_width
                        , opt_output_height
                );
                if (wall_real_w <= 0) wall_real_w = opt_display_width;
                if (wall_real_h <= 0) wall_real_h = opt_display_height;
                if (opt_output_width > 0) wall_real_w = opt_output_width;
                if (opt_output_height > 0) wall_real_h = opt_output_height;
                if (wall_real_w <= 0) wall_real_w = 1;
                if (wall_real_h <= 0) wall_real_h = 1;

                wall_w = wall_real_w;
                wall_h = wall_real_h;


                if (wall_w < 1) wall_w = 1;
                if (wall_h < 1) wall_h = 1;

                wall_image_aspect = (float) wall_w / (float) wall_h;

                log.d("wall_image=%dx%d", wall_w, wall_h);

                log.d("original size=%dx%dx%d(%.2fMB), factor=%s,resized=%dx%dx%d(%.2fMB)"
                        , check_option.outWidth
                        , check_option.outHeight
                        , pixel_bytes
                        , data_size / (float) (1024 * 1024)
                        , samplesize
                        , src_image.getWidth()
                        , src_image.getHeight()
                        , pixel_bytes2
                        , (src_image.getHeight() * row_bytes) / (float) (1024 * 1024)
                );
            }

            // レイアウトが完了するのを待つ
            while (!bCancelled) {
                if (flOuter.getWidth() > 0) break;
                waitEx(100);
            }
            if (bCancelled) return;

            // The size of the display frame
            int frame_w = flOuter.getWidth();
            int frame_h = flOuter.getHeight();
            float frame_aspect = frame_w / (float) frame_h;

            // The size of the data
            int src_w = src_image.getWidth();
            int src_h = src_image.getHeight();
            float src_aspect = src_w / (float) src_h;
            src_rect = new Rect(0, 0, src_w, src_h);

            // The size of the display image
            int shown_w;
            int shown_h;
            if (src_w <= frame_w && src_h <= frame_h) {
                //Scaling unnecessary
                shown_w = src_w;
                shown_h = src_h;
            } else if (src_aspect >= frame_aspect) {
                shown_w = frame_w;
                shown_h = (int) (0.5f + (src_h * frame_w) / (float) src_w);
            } else {
                // Image I fit in portrait. Vertical base than the display frame
                shown_h = frame_h;
                shown_w = (int) (0.5f + (src_w * frame_h) / (float) src_h);
            }

            // Position of the display image (display frame criteria)
            int x, y;
            x = (frame_w - shown_w) / 2;
            y = (frame_h - shown_h) / 2;
            shown_image_rect = new RectF(x, y, x + shown_w, y + shown_h);

            // Generate bitmap for display
            shown_image = Bitmap.createBitmap(frame_w, frame_h, MyApp.getBitmapConfig(src_image, Bitmap.Config.ARGB_8888));
            paint = new Paint();
            paint.setFilterBitmap(true);
            c = new Canvas(shown_image);
            cFrame = new Canvas(shown_image);
            c.drawBitmap(src_image, src_rect, shown_image_rect, paint);

            // Width and height of the selected range
            int selection_w;
            int selection_h;
            if (src_aspect <= wall_image_aspect) {
                // Image portrait than the ratio of wallpaper. It fits in right and left base
                selection_w = (int) shown_image_rect.width();
                selection_h = (int) (0.5 + selection_w / wall_image_aspect);
            } else {
                // Image is long in the transverse than wallpaper. It fits in the vertical base
                selection_h = (int) shown_image_rect.height();
                selection_w = (int) (0.5 + selection_h * wall_image_aspect);
            }

            x = (frame_w - selection_w) / 2;
            y = (frame_h - selection_h) / 2;
            prev_selection.set(x, y, x + selection_w, y + selection_h);

            ui_handler.post(new Runnable() {
                @Override
                public void run() {
                    if (bCancelled) return;
                    // 画像を表示
                    ivImage.setImageDrawable(new BitmapDrawable(getResources(), shown_image));


                    // Enable button
//                    tbOverall.setEnabled(true);
                    btnOk.setEnabled(true);
                    bLoading = false;
                    // Set selection
                    setSelection(
                            prev_selection.left
                            , prev_selection.top
                            , prev_selection.width()
                            , prev_selection.height()
                    );
                }
            });
        }
    }

    private void drawFrame(float x, float y, float w, float h) {
        Log.e("FRAME", x + " " + y + " " + w + " " + h);
        cFrame.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (c != null) {
            c.drawBitmap(src_image, src_rect, shown_image_rect, paint);
        }

        RectF partOne = new RectF(shown_image_rect.left, shown_image_rect.top, x, shown_image_rect.bottom);
        RectF partTwo = new RectF(x + w, shown_image_rect.top, shown_image_rect.right, shown_image_rect.bottom);
        RectF partThree = new RectF(x, shown_image_rect.top, x + w, y);
        RectF partFour = new RectF(x, y + h, x + w, shown_image_rect.bottom);

        final Paint mpaint = new Paint();
        mpaint.setStyle(Paint.Style.FILL);
        mpaint.setColor(Color.BLACK);
        mpaint.setAlpha(70);
        cFrame.drawRect(partOne, mpaint);
        cFrame.drawRect(partTwo, mpaint);
        cFrame.drawRect(partThree, mpaint);
        cFrame.drawRect(partFour, mpaint);
    }

    private void setSelection(int new_x, int new_y, int new_w, int new_h) {
        if (bLoading) return;
        if (!bEntire) {
            // Clip the width and height
            int max_w = (int) shown_image_rect.width();
            int max_h = (int) shown_image_rect.height();
            new_w = new_w > max_w ? max_w : new_w < 0 ? 0 : new_w;
            new_h = new_h > max_h ? max_h : new_h < 0 ? 0 : new_h;
            // Movable range
            int x_min = (int) shown_image_rect.left;
            int y_min = (int) shown_image_rect.top;
            int x_max = (int) shown_image_rect.right - new_w;
            int y_max = (int) shown_image_rect.bottom - new_h;
            //Clip position in the movable range
            new_x = new_x < x_min ? x_min : new_x > x_max ? x_max : new_x;
            new_y = new_y < y_min ? y_min : new_y > y_max ? y_max : new_y;

        } else {
            new_w = (int) shown_image_rect.width();
            new_h = (int) shown_image_rect.height();
            new_x = (int) shown_image_rect.left;
            new_y = (int) shown_image_rect.top;
        }


        // Update selection
        LinearLayout.LayoutParams lpSelection = (LinearLayout.LayoutParams) ivSelection.getLayoutParams();
        drawFrame(new_x, new_y, new_w, new_h);
        lpSelection.setMargins(
                new_x
                , new_y
                , flOuter.getWidth() - new_w - new_x
                , flOuter.getHeight() - new_h - new_y
        );
        ivSelection.requestLayout();
        Log.e("DRAW", "DRAW");

//        LinearLayout.LayoutParams lpSelection2 = (LinearLayout.LayoutParams) ivSelection2.getLayoutParams();
//        lpSelection2.setMargins(
//                new_x
//                , new_y
//                , flOuter.getWidth() - new_w - new_x
//                , flOuter.getHeight() - new_h - new_y
//        );
//        ivSelection2.requestLayout();


    }

    // Wallpaper configuration tasks
    WallpaperTask wp_task;

    class WallpaperTask extends WorkerBase {
        float ratioTemp = 1;

        @Override
        public void cancel() {
            // This task can not be canceled
        }

        @Override
        public void run() {
            if (bStandard) {
                wall_real_w *= standardRatio;
                wall_w *= standardRatio;
                wall_image = Bitmap.createBitmap(
                        wall_real_w
                        , wall_real_h
                        , MyApp.getBitmapConfig(src_image, Bitmap.Config.ARGB_8888)
                );
            } else if (bEntire && isScrollable) {
                Log.e("SET_WALL", isScrollable + "");
                ratioTemp = (float) (src_image.getWidth()) / (float) (src_image.getHeight());
                ratioTemp = ratioTemp / ((float) (opt_display_width) / (float) (opt_display_height));
                wall_real_w *= ratioTemp;
                wall_w *= ratioTemp;
                wall_image = Bitmap.createBitmap(
                        wall_real_w
                        , wall_real_h
                        , MyApp.getBitmapConfig(src_image, Bitmap.Config.ARGB_8888)
                );
            } else {
                wall_image = Bitmap.createBitmap(
                        wall_real_w
                        , wall_real_h
                        , MyApp.getBitmapConfig(src_image, Bitmap.Config.ARGB_8888)
                );
            }

            c = new Canvas(wall_image);
            paint = new Paint();
            paint.setFilterBitmap(true);
            if (bEntire) {
                if (isScrollable) {
                    Log.e("SET_WALL", isScrollable + "");
                    // Convert selection range in the display frame criteria, the selection range of the display image reference
                    double ratio_x = src_image.getWidth() / (double) shown_image_rect.width();
                    double ratio_y = src_image.getHeight() / (double) shown_image_rect.height();
                    LinearLayout.LayoutParams lpSelection = (LinearLayout.LayoutParams) ivSelection.getLayoutParams();
                    int x = lpSelection.leftMargin - (int) shown_image_rect.left;
                    int y = lpSelection.topMargin - (int) shown_image_rect.top;
                    Rect selection = new Rect(
                            (int) (0.5 + ratio_x * x)
                            , (int) (0.5 + ratio_y * y)
                            , (int) (0.5 + ratio_x * (x + ivSelection.getWidth()))
                            , (int) (0.5 + ratio_y * (y + ivSelection.getHeight()))
                    );
                    // 入力画像をリサイズ
                    RectF wall_rect = new RectF(0, 0, wall_w, wall_h);
                    c.drawBitmap(src_image, selection, wall_rect, paint);
                } else {
                    float x_ratio = src_image.getWidth() / (float) wall_w;
                    float y_ratio = src_image.getHeight() / (float) wall_h;
                    int w, h;
                    if (x_ratio >= y_ratio) {
                        h = (int) (0.5f + wall_w * src_image.getHeight() / (float) src_image.getWidth());
                        w = wall_w;
                    } else {
                        w = (int) (0.5f + wall_h * src_image.getWidth() / (float) src_image.getHeight());
                        h = wall_h;
                    }
                    int x = (wall_w - w) / 2;
                    int y = (wall_h - h) / 2;

                    // And resize the input image
                    Rect selection = new Rect(0, 0, src_image.getWidth(), src_image.getHeight());
                    RectF wall_rect = new RectF(x, y, x + w, y + h);
                    c.drawBitmap(src_image, selection, wall_rect, paint);
                }
            } else if (bFixed) {
                // Convert selection range in the display frame criteria, the selection range of the display image reference
                double ratio_x = src_image.getWidth() / (double) shown_image_rect.width();
                double ratio_y = src_image.getHeight() / (double) shown_image_rect.height();
                LinearLayout.LayoutParams lpSelection = (LinearLayout.LayoutParams) ivSelection.getLayoutParams();
                int x = lpSelection.leftMargin - (int) shown_image_rect.left;
                int y = lpSelection.topMargin - (int) shown_image_rect.top;
                Rect selection = new Rect(
                        (int) (0.5 + ratio_x * x)
                        , (int) (0.5 + ratio_y * y)
                        , (int) (0.5 + ratio_x * (x + ivSelection.getWidth()))
                        , (int) (0.5 + ratio_y * (y + ivSelection.getHeight()))
                );

                // And resize the input image
                RectF wall_rect = new RectF(0, 0, wall_w, wall_h);
                c.drawBitmap(src_image, selection, wall_rect, paint);
            } else if (bStandard) {
                // Convert selection range in the display frame criteria, the selection range of the display image reference
                double ratio_x = src_image.getWidth() / (double) shown_image_rect.width();
                double ratio_y = src_image.getHeight() / (double) shown_image_rect.height();
                LinearLayout.LayoutParams lpSelection = (LinearLayout.LayoutParams) ivSelection.getLayoutParams();
                int x = lpSelection.leftMargin - (int) shown_image_rect.left;
                int y = lpSelection.topMargin - (int) shown_image_rect.top;
                Rect selection = new Rect(
                        (int) (0.5 + ratio_x * x)
                        , (int) (0.5 + ratio_y * y)
                        , (int) (0.5 + ratio_x * (x + ivSelection.getWidth()))
                        , (int) (0.5 + ratio_y * (y + ivSelection.getHeight()))
                );
                // And resize the input image
                RectF wall_rect = new RectF(0, 0, wall_w, wall_h);
                c.drawBitmap(src_image, selection, wall_rect, paint);
            }
            //
//            src_image.recycle();
            ui_handler.post(new Runnable() {
                @Override
                public void run() {
                    if (isFinishing()) return;
                    try {
                        wpm.clear();
                        wpm.setBitmap(wall_image);
                    } catch (IOException ex) {
                        Toast.makeText(SetWallpaperActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                    } finally {
                        dialog.dismiss();
                        dialog = null;
                        wp_task = null;
                        SnackbarManager.show(
                                SnackBar.with(mContext)
                                        .text("Wallpaper Set Finished"));
                        finish();
//                        bLoading = true;
//                        isWallpaperSet = true;
//                        reloadActivity();
//                        bFixed = false;
//                        fixedWallpaperOption();
//                        loader_worker = new ImageLoaderWorker();
//                        loader_worker.start();
                    }
                }
            });
        }
    }

    private void initAdView() {
        mAdView = (AdView) findViewById(R.id.adViewSetWallpaper);
        mAdView.setVisibility(View.INVISIBLE);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        log.d("onCreate");
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            Window w = getWindow();
//            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }
        init_resource();
        init_page(getIntent());
        initAdView();
    }

    @Override
    protected void onDestroy() {
        log.d("onDestroy");
        super.onDestroy();
        if (src_image != null) src_image.recycle();
        if (mAdView != null) {
            mAdView.destroy();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        log.d("onNewIntent");
        super.onNewIntent(intent);
        init_page(intent);
    }

    @Override
    protected void onResume() {
        log.d("onResume");
        super.onResume();
        tracking_mode = TRACK_NONE;
        if (bLoading) {
            loader_worker = new ImageLoaderWorker();
            loader_worker.start();
        }
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onPause() {
        log.d("onPause");
        super.onPause();
        if (wp_task != null) wp_task.joinLoop(log, "wp_task");
        if (dialog != null) dialog.dismiss();
        if (loader_worker != null) loader_worker.joinLoop(log, "loader_worker");

        if (mAdView != null) {
            mAdView.pause();
        }
        if (isWallpaperSet) {
            isWallpaperSet = false;
        } else {
            overridePendingTransition(R.anim.set_as_slide_in, R.anim.set_as_slide_out);
        }
    }

    private void reloadActivity() {

        recreate();

//        Intent intent = getIntent();
////        overridePendingTransition(0, 0);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//        finish();
////        overridePendingTransition(0, 0);
//        startActivity(intent);
//        overridePendingTransition(R.anim.alpha_anim, R.anim.alpha_anim_out);

    }
}
