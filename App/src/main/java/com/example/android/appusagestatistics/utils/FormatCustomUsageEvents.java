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
    private static List<UsageEvents.Event> getUsageEvents(UsageStatsManager mUsageStatsManager) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);

        UsageEvents queryUsageEvents = mUsageStatsManager
                .queryEvents(cal.getTimeInMillis(),
                        System.currentTimeMillis());

        if (!queryUsageEvents.hasNextEvent())
            return null;

        UsageEvents.Event event = new UsageEvents.Event();
        List<UsageEvents.Event> events = new ArrayList<>();
        while (queryUsageEvents.getNextEvent(event)) {
            events.add(event);
            event = new UsageEvents.Event();
        }

        Log.i("GAAH", "getUsageEvents: size " + events.size());
        return events;
    }

    private static List<CustomUsageEvents> getCustomUsage(List<UsageEvents.Event> usageStatsList) {
        List<CustomUsageEvents> copy = new ArrayList<>();
        for (int i = 0; i < usageStatsList.size(); i++) {
            String eventType;

            if (usageStatsList.get(i).getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                eventType = Constants.FG;
                copy.add(new CustomUsageEvents(usageStatsList.get(i).getPackageName(),
                        eventType, usageStatsList.get(i).getTimeStamp()));
            } else if (usageStatsList.get(i).getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                eventType = Constants.BG;
                copy.add(new CustomUsageEvents(usageStatsList.get(i).getPackageName(),
                        eventType, usageStatsList.get(i).getTimeStamp()));
            }
        }
        return copy;
    }

    private static List<CustomUsageEvents> removeOld(List<CustomUsageEvents> events) {
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
        Log.i("GAAH2", "iteratorMergeSame: new Size " + events.size());
        return events;
    }

    public static List<DisplayUsageEvents> getDisplayUsageEventsList(UsageStatsManager mUsageStatsManager) {
        List<UsageEvents.Event> usageStatsList = getUsageEvents(mUsageStatsManager);
        if (usageStatsList == null)
            return null;
        List<CustomUsageEvents> copy = getCustomUsage(usageStatsList);

        Collections.sort(copy, new TimestampComparator());
        copy = FormatCustomUsageEvents.removeOld(copy);
        List<DisplayUsageEvents> displayUsageEventsList = mergeBgFg(copy);
        displayUsageEventsList = mergeSame(displayUsageEventsList);
        return displayUsageEventsList;
    }
}
