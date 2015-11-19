package com.zertinteractive.wallpaper.library.snackbar.listeners;

import com.zertinteractive.wallpaper.library.snackbar.SnackBar;

/**
 * Interface used to notify of all {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} display events. Useful if you want
 * to move other views while the Snackbar is on screen.
 */
public interface EventListener {
    /**
     * Called when a {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} is about to enter the screen
     *
     * @param snackBar the {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} that's being shown
     */
    public void onShow(SnackBar snackBar);

    /**
     * Called when a {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} is about to enter the screen while
     * a {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} is about to exit the screen by replacement.
     *
     * @param snackBar the {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} that's being shown
     */
    public void onShowByReplace(SnackBar snackBar);

    /**
     * Called when a {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} is fully shown
     *
     * @param snackBar the {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} that's being shown
     */
    public void onShown(SnackBar snackBar);

    /**
     * Called when a {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} is about to exit the screen
     *
     * @param snackBar the {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} that's being dismissed
     */
    public void onDismiss(SnackBar snackBar);

    /**
     * Called when a {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} is about to exit the screen
     * when a new {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} is about to enter the screen.
     *
     * @param snackBar the {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} that's being dismissed
     */
    public void onDismissByReplace(SnackBar snackBar);

    /**
     * Called when a {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} had just been dismissed
     *
     * @param snackBar the {@link com.zertinteractive.wallpaper.library.snackbar.SnackBar} that's being dismissed
     */
    public void onDismissed(SnackBar snackBar);
}
