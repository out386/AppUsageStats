package com.example.android.appusagestatistics.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.graphics.drawable.Drawable;

@Entity(tableName = "events", primaryKeys = {"startTime", "appName"})
public class DisplayEventEntity {
    public String appName;
    public long startTime;
    public long endTime;
    public int ongoing;
    @Ignore
    public String packageName;
    @Ignore
    public Drawable appIcon;

    public DisplayEventEntity() {

    }

    public DisplayEventEntity(String packageName, long startTime, long endTime) {
        this.packageName = packageName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public DisplayEventEntity(String packageName, long startTime, int ongoing) {
        this.packageName = packageName;
        this.startTime = startTime;
        this.ongoing = ongoing;
    }
}
