package com.example.android.appusagestatistics.utils;

import android.util.Log;

import com.example.android.appusagestatistics.models.CustomUsageEvents;

import java.util.ArrayList;
import java.util.Calendar;
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

        for (int i = 0; i < events.size() - 1; i++) {
            CustomUsageEvents thisEvent = events.get(i);
            CustomUsageEvents nextEvent = events.get(i + 1);
            if (thisEvent.packageName.equals(nextEvent.packageName))
                if (Constants.BG.equals(thisEvent.eventType)
                        && Constants.FG.equals(nextEvent.eventType))
                    copy.add(new DisplayUsageEvents(thisEvent.packageName,
                            nextEvent.timestamp, thisEvent.timestamp));
            else
                copy.add(new DisplayUsageEvents(thisEvent.packageName, true));
        }
        Log.i("GAAH2", "mergeBgFg: Original Size/2 " + events.size()/2);
        Log.i("GAAH2", "mergeBgFg: new size " + copy.size());
        return copy;
    }
}
