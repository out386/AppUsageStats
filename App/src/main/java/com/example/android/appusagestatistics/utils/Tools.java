package com.example.android.appusagestatistics.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.view.Display;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

    public static String formatTotalTime(long startMillis, long endMillis) {
        SimpleDateFormat sdfSpaces = new SimpleDateFormat("H m s");
        SimpleDateFormat sdfSingular = new SimpleDateFormat("'$'H 'hour,' '$'m 'minute, and' '$'s 'second'");
        Date date = new Date(endMillis - startMillis);

        sdfSingular.setTimeZone(TimeZone.getTimeZone("UTC"));
        sdfSpaces.setTimeZone(TimeZone.getTimeZone("UTC"));

        String[] formattedSpaces = sdfSpaces.format(date).split(" ");

        int i = 0;
        byte pluralH = 0;
        byte pluralM = 0;
        byte pluralS = 0;
        for (String current : formattedSpaces) {
            switch (i++) {
                case 0:
                    int elementH = Integer.parseInt(current);
                    if (elementH > 1)
                        pluralH = 1;
                    else if (elementH == 0)
                        pluralH = 2;
                    break;
                case 1:
                    int elementM = Integer.parseInt(current);
                    if (elementM > 1)
                        pluralM = 1;
                    else if (elementM == 0)
                        pluralM = 2;
                    break;
                case 2:
                    int elementS = Integer.parseInt(current);
                    if (elementS > 1)
                        pluralS = 1;
                    else if (elementS == 0)
                        pluralS = 2;
                    break;
            }
        }

        String formatted = sdfSingular.format(date);

        if (pluralH == 1)
            formatted = formatted.replace("hour", "hours");
        else if (pluralH == 2)
            formatted = formatted.replace("hour, ", "");
        if (pluralM == 1)
            formatted = formatted.replace("minute", "minutes");
        else if (pluralM == 2)
            formatted = formatted.replace("minute, ", "");
        if (pluralS == 1)
            formatted = formatted.replace("second", "seconds");
        else if (pluralS == 2)
            formatted = formatted.replace("second", "");

        if (pluralH == 2 && pluralM == 2)
            formatted = formatted.replace("and", "");

        formatted = formatted.replace("$0", "");
        formatted = formatted.replace("$", "");

        return formatted;
    }
}
