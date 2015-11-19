package com.zertinteractive.wallpaper.searchs;

import java.io.Serializable;

public class Settings implements Serializable {

    private static final long serialVersionUID = 2081909167593883030L;

    private String siteFilter;
    private String color;
    private String size;
    private String type;

    public Settings(String color, String size, String type, String siteFilter) {
        super();
        this.siteFilter = siteFilter;
        this.color = color;
        this.size = size;
        this.type = type;
    }

    public Settings() {

    }

    public String getSiteFilter() {
        return siteFilter;
    }

    public String getColor() {
        return color;
    }

    public String getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public void setSiteFilter(String siteFilter) {
        this.siteFilter = siteFilter;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQueryString() {
        String result = "";
        if (this.size != null) {
            result += ("&imgsz=" + this.size);
        }
        if (this.type != null) {
            result += ("&imgtype=" + this.type);
        }
        if (this.color != null) {
            result += ("&imgcolor=" + this.color);
        }
        if (this.siteFilter != null && this.siteFilter.length() > 0) {
            result += ("&as_sitesearch=" + this.siteFilter);
        }

        return result;

    }

}
