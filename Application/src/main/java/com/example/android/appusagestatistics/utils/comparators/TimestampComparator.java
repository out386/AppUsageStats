package com.example.android.appusagestatistics.utils.comparators;

import android.app.usage.UsageStats;

import com.example.android.appusagestatistics.models.CustomUsageEvents;

import java.util.Comparator;

/**
 * The {@link Comparator} to sort a collection of {@link UsageStats} sorted by the total
 * time in foreground the app was in the descendant order.
 */
public class TimestampComparator implements Comparator<CustomUsageEvents> {

    @Override
    public int compare(CustomUsageEvents left, CustomUsageEvents right) {
        return Long.compare(right.timestamp, left.timestamp);
    }
}

