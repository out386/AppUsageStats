package com.example.android.appusagestatistics.models;

import android.graphics.drawable.Drawable;

public class DisplayUsageEvent {
    public String appName;
    public String packageName;
    public Drawable appIcon;
    public long startTime;
    public long endTime;
    public boolean ongoing;

    public DisplayUsageEvent(String packageName, long startTime, long endTime) {
        this.packageName = packageName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public DisplayUsageEvent(String packageName, long startTime, boolean ongoing) {
        this.packageName = packageName;
        this.startTime = startTime;
        this.ongoing = ongoing;
    }
}
