package com.example.android.appusagestatistics.comparators;

import android.app.usage.UsageStats;

import com.example.android.appusagestatistics.models.CustomUsageStats;

import java.util.Comparator;

public class Comparators {

    /**
     * The {@link Comparator} to sort a collection of {@link UsageStats} sorted by the total
     * time in foreground the app was in the descendant order.
     */
    public static class TotalTimeComparatorDesc implements Comparator<CustomUsageStats> {

        @Override
        public int compare(CustomUsageStats left, CustomUsageStats right) {
            return Long.compare(right.totalTimeInForeground, left.totalTimeInForeground);
        }
    }

    /**
     * The {@link Comparator} to sort a collection of {@link UsageStats} sorted by the package name
     * in the descendant order.
     */
    public static class PackageNameComparatorDesc implements Comparator<UsageStats> {

        @Override
        public int compare(UsageStats left, UsageStats right) {
            return right.getPackageName().compareTo(left.getPackageName());
        }
    }

}
