package com.example.android.appusagestatistics.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    public static String findLauncher(Context context) {
        PackageManager localPackageManager = context.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        return localPackageManager.resolveActivity(intent,
                PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
    }
}
