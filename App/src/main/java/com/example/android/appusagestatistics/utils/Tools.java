package com.example.android.appusagestatistics.utils;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;

import com.example.android.appusagestatistics.activities.AppUsageStatisticsActivity;

/**
 * Created by j on 12/7/17.
 */

public class Tools {
    public static DisplaySize getDisplaySizes(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return new DisplaySize(size.x, size.y);
    }
}
