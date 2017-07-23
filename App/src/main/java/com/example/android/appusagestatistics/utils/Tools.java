package com.example.android.appusagestatistics.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Display;

import com.example.android.appusagestatistics.models.AppFilteredEvents;
import com.example.android.appusagestatistics.models.DisplayEventEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lecho.lib.hellocharts.util.ChartUtils;

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

    public static String formatTotalTime(long startMillis, long endMillis, boolean needSeconds) {
        SimpleDateFormat sdfSpaces = new SimpleDateFormat("H m s");
        SimpleDateFormat sdfSingular;
        if (needSeconds)
            sdfSingular = new SimpleDateFormat("'$'H 'hour,' '$'m 'minute, and' '$'s 'second'");
        else
            sdfSingular = new SimpleDateFormat("'$'H 'hour, and' '$'m 'minute'");
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
        else if (pluralS == 2) {
            formatted = formatted.replace("second", "");
            formatted = formatted.replace(", and", "");
        }

        if (!needSeconds && pluralH == 2) {
            if (pluralM == 2)
                formatted = null;
            else
                formatted = formatted.replace("and", "");
        } else if (needSeconds && pluralH == 2 && pluralM == 2) {
            if (pluralS == 2)
                formatted = null;
            else
                formatted = formatted.replace("and", "");
        }

        if (formatted != null) {
            formatted = formatted.replace("$0", "");
            formatted = formatted.replace("$", "");
        }

        return formatted;
    }

    public static AppFilteredEvents getSpecificAppEvents(List<DisplayEventEntity> allEvents, String appName) {
        AppFilteredEvents appFilteredEvents = new AppFilteredEvents();
        for (DisplayEventEntity event : allEvents) {
            if (appName.equals(event.appName))
                appFilteredEvents.appEvents.add(event);
            else
                appFilteredEvents.otherEvents.add(event);
        }
        return appFilteredEvents;
    }

    public static long findTotalUsage(List<DisplayEventEntity> events) {
        long totalUsage = 0;
        for (DisplayEventEntity event : events) {
            if (event.endTime == 0)
                continue;
            totalUsage += event.endTime - event.startTime;
        }
        return totalUsage;
    }

    public static int[] generateRandomColours(int count) {

        final int COLOR_BLUE = Color.parseColor("#33B5E5");
        final int COLOR_VIOLET = Color.parseColor("#AA66CC");
        final int COLOR_ORANGE = Color.parseColor("#FFBB33");
        final int COLOR_RED = Color.parseColor("#FF4444");

        final int [] COLORS = new int[]{COLOR_BLUE, COLOR_VIOLET, COLOR_ORANGE, COLOR_RED};

        int [] colours = new int[count];
        int numberGenerated = 0;
        while (numberGenerated <= count) {
            colours[numberGenerated] = COLORS[(int)Math.round(Math.random() * (COLORS.length - 1))];
            if (count >= 3 && count <= COLORS.length - 1 && numberGenerated != 0) {
                int nextIndex;
                if (numberGenerated + 1 < count)
                    nextIndex = numberGenerated + 1;
                else
                    nextIndex = 0;
            }
            numberGenerated ++;
        }
        return colours;
    }
}
