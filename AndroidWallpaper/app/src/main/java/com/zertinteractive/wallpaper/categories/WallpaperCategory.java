package com.zertinteractive.wallpaper.categories;

/**
 * Created by Dell on 10/25/2015.
 */
public enum WallpaperCategory {

    ALL("ALL"),
    HAPPY("HAPPY"),
    SAD("SAD"),
    SURPRISE("SURPRISE"),
    ANGRY("ANGRY"),
    FUNNY("FUNNY"),
    AMAZED("AMAZED"),
    DAILY("DAILY"),
    FEATURED("FEATURED"),
    FAVOURITE("FAVOURITE"),
    SEARCH("SEARCH");


    private final String wallpaperCategory;

    WallpaperCategory(String wallpaperCategory) {
        this.wallpaperCategory = wallpaperCategory;
    }
}