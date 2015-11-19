package com.zertinteractive.wallpaper.library.snackbar;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

/**
 * A handler for multiple {@link SnackBar}s
 */
public class SnackbarManager {

    private static final String TAG = SnackbarManager.class.getSimpleName();
    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());

    private static WeakReference<SnackBar> snackbarReference;

    private SnackbarManager() {
    }

    /**
     * Displays a {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} in the current {@link Activity}, dismissing
     * the current Snackbar being displayed, if any. Note that the Activity will be obtained from
     * the Snackbar's {@link android.content.Context}. If the Snackbar was created with
     * {@link Activity#getApplicationContext()} then you must explicitly pass the target
     * Activity using {@link #show(SnackBar, Activity)}
     *
     * @param snackBar instance of {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} to display
     */
    public static void show(@NonNull SnackBar snackBar) {
        try {
            show(snackBar, (Activity) snackBar.getContext());
        } catch (ClassCastException e) {
            Log.e(TAG, "Couldn't get Activity from the Snackbar's Context. Try calling " +
                    "#show(Snackbar, Activity) instead", e);
        }
    }

    /**
     * Displays a {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} in the current {@link Activity}, dismissing
     * the current Snackbar being displayed, if any
     *
     * @param snackBar instance of {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} to display
     * @param activity target {@link Activity} to display the Snackbar
     */
    public static void show(@NonNull final SnackBar snackBar, @NonNull final Activity activity) {
        MAIN_THREAD.post(new Runnable() {
            @Override
            public void run() {
                SnackBar currentSnackBar = getCurrentSnackbar();
                if (currentSnackBar != null) {
                    if (currentSnackBar.isShowing() && !currentSnackBar.isDimissing()) {
                        currentSnackBar.dismissAnimation(false);
                        currentSnackBar.dismissByReplace();
                        snackbarReference = new WeakReference<>(snackBar);
                        snackBar.showAnimation(false);
                        snackBar.showByReplace(activity);
                        return;
                    }
                    currentSnackBar.dismiss();
                }
                snackbarReference = new WeakReference<>(snackBar);
                snackBar.show(activity);
            }
        });
    }

    /**
     * Displays a {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} in the specified {@link ViewGroup}, dismissing
     * the current Snackbar being displayed, if any
     *
     * @param snackBar instance of {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} to display
     * @param parent   parent {@link ViewGroup} to display the Snackbar
     */
    public static void show(@NonNull SnackBar snackBar, @NonNull ViewGroup parent) {
        show(snackBar, parent, SnackBar.shouldUsePhoneLayout(snackBar.getContext()));
    }

    /**
     * Displays a {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} in the specified {@link ViewGroup}, dismissing
     * the current Snackbar being displayed, if any
     *
     * @param snackBar       instance of {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} to display
     * @param parent         parent {@link ViewGroup} to display the Snackbar
     * @param usePhoneLayout true: use phone layout, false: use tablet layout
     */
    public static void show(@NonNull final SnackBar snackBar, @NonNull final ViewGroup parent,
                            final boolean usePhoneLayout) {
        MAIN_THREAD.post(new Runnable() {
            @Override
            public void run() {
                SnackBar currentSnackBar = getCurrentSnackbar();
                if (currentSnackBar != null) {
                    if (currentSnackBar.isShowing() && !currentSnackBar.isDimissing()) {
                        currentSnackBar.dismissAnimation(false);
                        currentSnackBar.dismissByReplace();
                        snackbarReference = new WeakReference<>(snackBar);
                        snackBar.showAnimation(false);
                        snackBar.showByReplace(parent, usePhoneLayout);
                        return;
                    }
                    currentSnackBar.dismiss();
                }
                snackbarReference = new WeakReference<>(snackBar);
                snackBar.show(parent, usePhoneLayout);
            }
        });
    }

    /**
     * Dismisses the {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} shown by this manager.
     */
    public static void dismiss() {
        final SnackBar currentSnackBar = getCurrentSnackbar();
        if (currentSnackBar != null) {
            MAIN_THREAD.post(new Runnable() {
                @Override
                public void run() {
                    currentSnackBar.dismiss();
                }
            });
        }
    }

    /**
     * Return the current Snackbar
     */
    public static SnackBar getCurrentSnackbar() {
        if (snackbarReference != null) {
            return snackbarReference.get();
        }
        return null;
    }
}
