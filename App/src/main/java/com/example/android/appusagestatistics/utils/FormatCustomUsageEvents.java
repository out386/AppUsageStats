package com.example.android.appusagestatistics.utils;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.util.Log;

import com.example.android.appusagestatistics.models.CustomUsageEvents;
import com.example.android.appusagestatistics.models.DisplayUsageEvents;
import com.example.android.appusagestatistics.utils.comparators.TimestampComparator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by j on 4/7/17.
 */

public class FormatCustomUsageEvents {

    /**
     * Returns the list including the time span specified by the
     * intervalType argument.
     *
     * @return A list of {@link android.app.usage.UsageStats}.
     */
    private static List<CustomUsageEvents> getUsageEvents(UsageStatsManager mUsageStatsManager) {
        Calendar cal = Calendar.getInstance();
        List<CustomUsageEvents> copy = new ArrayList<>();
        UsageEvents.Event event = new UsageEvents.Event();
        String eventType;

        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        UsageEvents queryUsageEvents = mUsageStatsManager.queryEvents(cal.getTimeInMillis(),
                        System.currentTimeMillis());

        if (!queryUsageEvents.hasNextEvent())
            return null;

        while (queryUsageEvents.getNextEvent(event)) {
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                eventType = Constants.FG;
                copy.add(new CustomUsageEvents(event.getPackageName(),
                        eventType, event.getTimeStamp()));
            } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                eventType = Constants.BG;
                copy.add(new CustomUsageEvents(event.getPackageName(),
                        eventType, event.getTimeStamp()));
            }

            event = new UsageEvents.Event();
        }

        Log.i("GAAH", "getUsageEvents: size " + copy.size());
        return copy;
    }

    /*
    * Merges a background event and a foreground event of the same package to a CustomUsageEvents
    * Merging only happens if a FG immediately follows a BG
    */
    private static List<DisplayUsageEvents> mergeBgFg(List<CustomUsageEvents> events) {
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
            } else if (i == 0) {
                // Making sure that Ongoing events in the middle of the list get dropped
                copy.add(new DisplayUsageEvents(thisEvent.packageName, nextEvent.timestamp, true));
            }
        }
        Log.i("GAAH2", "mergeBgFg: Original Size/2 " + events.size() / 2);
        return copy;
    }


    /*
    * Merges items from the same package name together if the events are less than MIN_TIME_DIFFERENCE ms apart
    */
    private static List<DisplayUsageEvents> mergeSame(List<DisplayUsageEvents> events) {
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
        Log.i("GAAH2", "mergeSame: new Size " + events.size());
        return events;
    }

    public static List<DisplayUsageEvents> getDisplayUsageEventsList(UsageStatsManager mUsageStatsManager) {
        List<CustomUsageEvents> usageEvents = getUsageEvents(mUsageStatsManager);
        if (usageEvents == null)
            return null;

        Collections.sort(usageEvents, new TimestampComparator());
        List<DisplayUsageEvents> displayUsageEventsList = mergeBgFg(usageEvents);
        displayUsageEventsList = mergeSame(displayUsageEventsList);
        return displayUsageEventsList;
    }
}
