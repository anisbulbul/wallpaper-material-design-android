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

package com.zertinteractive.wallpaper.viewmodels;

import com.zertinteractive.wallpaper.categories.WallpaperCategory;

public class ViewModel {
    private String title;
    private WallpaperCategory wallpaperCategory;
    private WallpaperCategory wallpaperCategoryFrom;
    private int superCategory;
    private boolean isLoaded;
    private String imageSmall;
    private String imageBig;
    private String imageDownload;

    public ViewModel(WallpaperCategory wallpaperCategory, String imageSmall, String imageBig, String imageDownload) {
        super();
        this.title = "NoTitle";
        this.wallpaperCategory = wallpaperCategory;
        this.isLoaded = false;
        this.imageSmall = imageSmall;
        this.imageBig = imageBig;
        this.imageDownload = imageDownload;
    }

    public ViewModel(String title, WallpaperCategory wallpaperCategory, int superCategory,
                     String imageSmall, String imageBig, String imageDownload) {
        super();
        this.title = title;
        this.wallpaperCategory = wallpaperCategory;
        this.superCategory = superCategory;
        this.isLoaded = false;
        this.imageSmall = imageSmall;
        this.imageBig = imageBig;
        this.imageDownload = imageDownload;
    }

    public String getTite() {
        return title;
    }

    public WallpaperCategory getWallpaperCategory() {
        return wallpaperCategory;
    }

    public void setWallpaperCategory(WallpaperCategory wallpaperCategory) {
        this.wallpaperCategory = wallpaperCategory;
    }

    public int getSuperCategory() {
        return superCategory;
    }

    public boolean setIsLoaded(boolean isLoaded) {
        return this.isLoaded = isLoaded;
    }

    public boolean getIsLoaded() {
        return isLoaded;
    }

    public int setSuperCategory(int superCategory) {
        return this.superCategory = superCategory;
    }

    public String getImageSmall() {
        return imageSmall;
    }

    public String getImageBig() {
        return imageBig;
    }

    public String getImageDownload() {
        return imageDownload;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ViewModel other = (ViewModel) obj;
        if (imageSmall.equals(other.imageSmall))
            return false;
        return true;
    }
}
