package com.example.android.appusagestatistics.utils;

import android.util.Log;

import com.example.android.appusagestatistics.models.CustomUsageEvents;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.example.android.appusagestatistics.models.DisplayUsageEvents;
import com.example.android.appusagestatistics.utils.Constants;

/**
 * Created by j on 4/7/17.
 */

public class FormatCustomUsageEvents {

    public static List<CustomUsageEvents> removeOld(List<CustomUsageEvents> events) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long todayMillis = calendar.getTimeInMillis();
        List<CustomUsageEvents> copy = new ArrayList<>();
        for (CustomUsageEvents event : events)
            if (event.timestamp > todayMillis)
                copy.add(event);
        Log.i("GAAH", "removeOld: Original size " + events.size());
        Log.i("GAAH", "removeOld: Copy size " + copy.size());
        return copy;
    }

    public static List<DisplayUsageEvents> mergeBgFg(List<CustomUsageEvents> events) {
        List<DisplayUsageEvents> copy = new ArrayList<>();
//        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        //events = removeNulls(events);

        boolean skip = false;
        for (int i = 0; i < events.size() - 1; i++) {
            if (skip) {
                skip = false;
                continue;
            }

            CustomUsageEvents thisEvent = events.get(i);
            CustomUsageEvents nextEvent = events.get(i + 1);

//            Log.i("GAAH3", "mergeBgFg: this name " + thisEvent.packageName + " this time " + sdf.format(new Date(thisEvent.timestamp)) + " " + thisEvent.eventType);
//            Log.i("GAAH3", "mergeBgFg: next name " + nextEvent.packageName + "next time " + sdf.format(new Date(nextEvent.timestamp)) + " " + nextEvent.eventType);
            if (thisEvent.packageName.equals(nextEvent.packageName)) {
                if (Constants.BG.equals(thisEvent.eventType)
                        && Constants.FG.equals(nextEvent.eventType)) {
                    copy.add(new DisplayUsageEvents(thisEvent.packageName,
                            nextEvent.timestamp, thisEvent.timestamp));
                    skip = true;
                }
            } else
                    copy.add(new DisplayUsageEvents(thisEvent.packageName, nextEvent.timestamp, true));


//            if (copy.size() > 0)
//                Log.i("GAAH3", "mergeBgFg: copy name " + copy.get(copy.size() - 1).packageName
//                        + " copy start time " + sdf.format(new Date(copy.get(copy.size() - 1).startTime))
//                        + " copy end time " + sdf.format(new Date(copy.get(copy.size() - 1).endTime)));
        }
        Log.i("GAAH2", "mergeBgFg: Original Size/2 " + events.size() / 2);
        Log.i("GAAH2", "mergeBgFg: new size " + copy.size());
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
}
