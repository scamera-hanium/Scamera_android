package com.example.davichiar.scamera_android.NaverImagePrint;

import java.util.ArrayList;

public class NaverImage {
    private String lastBuildDate;
    private long total;
    private long start;
    private long display;
    private ArrayList<NaverImageItem> items;

    public NaverImage() {}

    public String getLastBuildDate() {
        return lastBuildDate;
    }

    public void setLastBuildDate(String lastBuildDate) {
        this.lastBuildDate = lastBuildDate;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getDisplay() {
        return display;
    }

    public void setDisplay(long display) {
        this.display = display;
    }

    public ArrayList<NaverImageItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<NaverImageItem> items) {
        this.items = items;
    }
}
