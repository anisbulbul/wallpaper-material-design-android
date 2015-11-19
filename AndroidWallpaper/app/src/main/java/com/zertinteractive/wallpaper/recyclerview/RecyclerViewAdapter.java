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

package com.zertinteractive.wallpaper.recyclerview;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nineoldandroids.animation.Animator;
import com.squareup.picasso.Picasso;
import com.zertinteractive.wallpaper.MainActivity;
import com.zertinteractive.wallpaper.R;
import com.zertinteractive.wallpaper.activities.DetailActivity;
import com.zertinteractive.wallpaper.categories.WallpaperCategory;
import com.zertinteractive.wallpaper.library.animations.Techniques;
import com.zertinteractive.wallpaper.library.animations.YoYo;
import com.zertinteractive.wallpaper.library.snackbar.SnackBar;
import com.zertinteractive.wallpaper.library.snackbar.SnackbarManager;
import com.zertinteractive.wallpaper.viewmodels.ViewModel;

import java.io.File;
import java.util.List;
import java.util.Random;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements View.OnClickListener {

    private List<ViewModel> items;
    private OnItemClickListener onItemClickListener;
    private Context context;
    private DownloadManager dm;
    private long enqueue;

    public RecyclerViewAdapter(List<ViewModel> items, Context context) {
        this.items = items;
        this.context = context;

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
//                    Query query = new Query();
//                    query.setFilterById(enqueue);
//                    Cursor c = dm.query(query);
//                    if (c.moveToFirst()) {
//                        int columnIndex = c
//                                .getColumnIndex(DownloadManager.COLUMN_STATUS);
////                        if (DownloadManager.STATUS_SUCCESSFUL == c
////                                .getInt(columnIndex)) {
////
//////                            ImageView view = (ImageView) findViewById(R.id.imageView1);
//////                            String uriString = c
//////                                    .getString(c
//////                                            .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
//////                            view.setImageURI(Uri.parse(uriString));
////                        }
//                    }
                }
            }
        };

        context.registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        context.unregisterReceiver(receiver);


    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler, parent, false);
        v.setOnClickListener(this);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ViewModel item = items.get(position);
        holder.image.setImageBitmap(null);
        if (item.getSuperCategory() == 3) {
            holder.imageFavourite.setImageDrawable(context.getResources().getDrawable(R.drawable.favorite_active));
        } else {
            holder.imageFavourite.setImageDrawable(context.getResources().getDrawable(R.drawable.favorite_inactive));
        }
        Picasso.with(holder.image.getContext())
                .load(item.getImageSmall())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(holder.image, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        item.setIsLoaded(true);
                        Palette.from(((BitmapDrawable) holder.image.getDrawable()).getBitmap()).
                                generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
//                                        holder.paletteLinearLayout.setBackgroundColor(
//                                                palette.getDarkMutedColor(
//                                                        context.getResources().getColor(R.color.palette_default_color)));
                                        holder.paletteLinearLayout.setBackgroundColor(context.getResources().getColor(R.color.action_bar_color));
                                    }
                                });


                    }

                    @Override
                    public void onError() {
//                        Log.e("POSITION : ", "ERROR IN " + position);
                    }
                });

        holder.itemView.setTag(item);
        holder.imageFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.getSuperCategory() != 3) {
                    item.setSuperCategory(3);
                    holder.imageFavourite.setImageDrawable(context.getResources().getDrawable(R.drawable.favorite_active));
                    MainActivity.resetWallpapers(position, 3, item);
                    SnackbarManager.show(
                            SnackBar.with(context)
                                    .text("Adding Favourite ..."));
                    YoYo.with(Techniques.ZoomIn)
                            .duration(500)
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

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            })
                            .playOn(holder.imageFavourite);
                } else {
                    item.setSuperCategory(0);
                    holder.imageFavourite.setImageDrawable(context.getResources().getDrawable(R.drawable.favorite_inactive));
                    MainActivity.resetWallpapers(position, 0, item);
                    SnackbarManager.show(
                            SnackBar.with(context)
                                    .text("Removing Favourite ..."));
                    YoYo.with(Techniques.ZoomIn)
                            .duration(500)
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

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            })
                            .playOn(holder.imageFavourite);
                }
            }
        });
        holder.imageDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Log.e("IMAGE", "" + item.getImageDownload());

                String path = Environment.getExternalStorageDirectory().toString();


                String fileName = item.getImageDownload();
                fileName = fileName.substring(fileName.length() - 15, fileName.length() - 9); // H_89.jpg
                if (!getPrefixName().equals(DetailActivity.SEARCH_WALLPAPER_PREFIX)) {
                    while (!DetailActivity.isDigit(fileName.charAt(0))) {
                        fileName = fileName.substring(1);
                    }
                } else {
                    fileName = Math.abs(new Random().nextInt()) + ".jpg";
                    while (new File(path, "/" + DetailActivity.MOOD_WALLPAPER_DIR + "/" + getPrefixName() + fileName).exists()) {
                        fileName = Math.abs(new Random().nextInt()) + ".jpg";
                    }
                }


                File file = new File(path, "/" + DetailActivity.MOOD_WALLPAPER_DIR + "/" + getPrefixName() + fileName);

                if (file.getAbsoluteFile().exists()) {
                    SnackbarManager.show(
                            SnackBar.with(context)
                                    .text("ALLREADY DOWNLOADED: " + getPrefixName() + " : " + fileName));
                } else {
                    dm = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(
                            Uri.parse(item.getImageDownload()));
                    request.setTitle("Mood Wallpaper");
                    request.setDescription(file.getAbsolutePath());
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(DetailActivity.MOOD_WALLPAPER_DIR, getPrefixName() + fileName);
                    enqueue = dm.enqueue(request);
                    SnackbarManager.show(
                            SnackBar.with(context)
                                    .text("DOWNLOADING ... : " + getPrefixName() + " : " + fileName));
                }
            }
        });

    }

    public String getPrefixName() {
        if (MainActivity.getCurrentWallpaperCategory() == WallpaperCategory.HAPPY) {
            return DetailActivity.HAPPY_WALLPAPER_PREFIX;
        } else if (MainActivity.getCurrentWallpaperCategory() == WallpaperCategory.SAD) {
            return DetailActivity.SAD_WALLPAPER_PREFIX;
        } else if (MainActivity.getCurrentWallpaperCategory() == WallpaperCategory.SURPRISE) {
            return DetailActivity.SURPRISE_WALLPAPER_PREFIX;
        } else if (MainActivity.getCurrentWallpaperCategory() == WallpaperCategory.ANGRY) {
            return DetailActivity.ANGRY_WALLPAPER_PREFIX;
        } else if (MainActivity.getCurrentWallpaperCategory() == WallpaperCategory.FUNNY) {
            return DetailActivity.FUNNY_WALLPAPER_PREFIX;
        } else if (MainActivity.getCurrentWallpaperCategory() == WallpaperCategory.AMAZED) {
            return DetailActivity.AMAZED_WALLPAPER_PREFIX;
        } else if (MainActivity.getCurrentWallpaperCategory() == WallpaperCategory.DAILY) {
            return DetailActivity.DAILY_WALLPAPER_PREFIX;
        } else if (MainActivity.getCurrentWallpaperCategory() == WallpaperCategory.FEATURED) {
            return DetailActivity.FEATURED_WALLPAPER_PREFIX;
        } else if (MainActivity.getCurrentWallpaperCategory() == WallpaperCategory.FAVOURITE) {
            return DetailActivity.FAVOURITE_WALLPAPER_PREFIX;
        } else if (MainActivity.getCurrentWallpaperCategory() == WallpaperCategory.SEARCH) {
            return DetailActivity.SEARCH_WALLPAPER_PREFIX;
        }
        return DetailActivity.ALL_WALLPAPER_PREFIX;
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onClick(final View v) {
        if (onItemClickListener != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ViewModel viewModel = (ViewModel) v.getTag();
                    if (viewModel.getIsLoaded()) {
                        onItemClickListener.onItemClick(v, viewModel);
                    }
                }
            }, 100);
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public ImageView imageDownload;
        public ImageView imageFavourite;
        public LinearLayout paletteLinearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            imageDownload = (ImageView) itemView.findViewById(R.id.downloadImage);
            imageFavourite = (ImageView) itemView.findViewById(R.id.favouriteImage);
            paletteLinearLayout = (LinearLayout) itemView.findViewById(R.id.paletteColor);

//            final LinearLayout paletteLinearLayout = (LinearLayout) itemView.findViewById(R.id.paletteColor);
//            Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
//
//            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
//                public void onGenerated(Palette palette) {
//                    int lightVibrantColor = palette.getLightVibrantColor(context.getResources().getColor(android.R.color.white));
//                    int vibrantColor = palette.getVibrantColor(context.getResources().getColor(R.color.accent));
//                    paletteLinearLayout.setBackgroundColor(vibrantColor);
//                }
//            });

//            paletteLinearLayout.setBackgroundColor(Color.RED);
//            imageDownload.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Log.e("CLICK", "DOWNLOAD : ");
//                }
//            });
        }
    }

    public interface OnItemClickListener {

        void onItemClick(View view, ViewModel viewModel);

    }
}
