package com.zertinteractive.wallpaper.library.snackbar.listeners;

import com.zertinteractive.wallpaper.library.snackbar.SnackBar;

/**
 * This adapter class provides empty implementations of the methods from {@link com.zertinteractive.wallpaper.library.snackbar.listeners.EventListener}.
 * If you are only interested in a subset of the interface methods you can extend this class an override only the methods you need.
 */
public abstract class EventListenerAdapter implements EventListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShow(SnackBar snackBar) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowByReplace(SnackBar snackBar) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onShown(SnackBar snackBar) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDismiss(SnackBar snackBar) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDismissByReplace(SnackBar snackBar) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDismissed(SnackBar snackBar) {

    }
}
