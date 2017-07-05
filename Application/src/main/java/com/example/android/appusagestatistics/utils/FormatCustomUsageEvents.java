package com.example.android.appusagestatistics.utils;

import android.util.Log;

import com.example.android.appusagestatistics.models.CustomUsageEvents;
import com.example.android.appusagestatistics.models.DisplayUsageEvents;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Created by j on 4/7/17.
 */

public class FormatCustomUsageEvents {

    public static List<CustomUsageEvents> removeOld(List<CustomUsageEvents> events) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long todayMillis = calendar.getTimeInMillis();
        List<CustomUsageEvents> copy = new ArrayList<>();
        for (CustomUsageEvents event : events)
            if (event.timestamp >= todayMillis) {
                copy.add(event);
            }
        Log.i("GAAH", "removeOld: Original size " + events.size());
        Log.i("GAAH", "removeOld: Copy size " + copy.size());
        return copy;
    }

    /*
    * Merges a background event and a foreground event of the same package to a CustomUsageEvents
    * Merging only happens if a FG immediately follows a BG
    */
    public static List<DisplayUsageEvents> mergeBgFg(List<CustomUsageEvents> events) {
        List<DisplayUsageEvents> copy = new ArrayList<>();
        boolean skip = false;

        for (int i = 0; i < events.size() - 1; i++) {
            if (skip) {
                skip = false;
                continue;
            }

            CustomUsageEvents thisEvent = events.get(i);
            CustomUsageEvents nextEvent = events.get(i + 1);

            if (thisEvent.packageName.equals(nextEvent.packageName)) {
                if (Constants.BG.equals(thisEvent.eventType)
                        && Constants.FG.equals(nextEvent.eventType)) {
                    copy.add(new DisplayUsageEvents(thisEvent.packageName,
                            nextEvent.timestamp, thisEvent.timestamp));
                    skip = true;
                }
            } else
                copy.add(new DisplayUsageEvents(thisEvent.packageName, nextEvent.timestamp, true));
        }
        Log.i("GAAH2", "mergeBgFg: Original Size/2 " + events.size() / 2);
        return removeDuplicateOngoing(copy);
    }

    private static List<DisplayUsageEvents> removeDuplicateOngoing(List<DisplayUsageEvents> events) {
        // We want to filter out "ongoing"s from the middle of the list, as those are mistakes caused by getUsageEvents
        List<DisplayUsageEvents> copy = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).ongoing && i > 0)
                continue;
            copy.add(events.get(i));
        }
        return copy;
    }


    /*
    * Merges items from the same package name together if the events are less than 5 seconds apart
    */
    public static List<DisplayUsageEvents> mergeSame(List<DisplayUsageEvents> events) {
        final long MIN_TIME_DIFFERENCE = 1000 * 5;

        DisplayUsageEvents previous = null;
        Iterator<DisplayUsageEvents> iterator = events.iterator();
        while (iterator.hasNext()) {
            if (previous == null) {
                previous = iterator.next();
                continue;
            }

            DisplayUsageEvents thisEvent = iterator.next();

            // THIS WILL MISS THE LAST EVENT
            if (previous.packageName.equals(thisEvent.packageName)) {
                if (previous.startTime - thisEvent.endTime > MIN_TIME_DIFFERENCE) {
                    previous = thisEvent;
                } else {
                    previous.startTime = thisEvent.startTime;
                    iterator.remove();
                }
            } else
                previous = thisEvent;

        }
        Log.i("GAAH2", "iteratorMergeSame: new Size " + events.size());
        return events;
    }
}
