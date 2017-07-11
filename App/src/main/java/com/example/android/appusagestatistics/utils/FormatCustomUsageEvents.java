package com.example.android.appusagestatistics.utils;

import android.app.Application;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Room;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.android.appusagestatistics.R;
import com.example.android.appusagestatistics.database.Database;
import com.example.android.appusagestatistics.models.CustomUsageEvents;
import com.example.android.appusagestatistics.models.DisplayEventEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by j on 4/7/17.
 */

public class FormatCustomUsageEvents extends AndroidViewModel {

    private MutableLiveData<List<DisplayEventEntity>> displayLiveData = new MutableLiveData<>();
    private Database db;

    public FormatCustomUsageEvents(Application application) {
        super(application);
    }

    /**
     * Returns the list including the time span specified by the
     * intervalType argument.
     *
     * @return A list of {@link android.app.usage.UsageStats}.
     */
    private List<CustomUsageEvents> getUsageEvents(UsageStatsManager mUsageStatsManager,
                                                   String[] excludePackages) {
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

        outer:
        while (queryUsageEvents.getNextEvent(event)) {
            for (String excludePackage : excludePackages)
                if (excludePackage.equals(event.getPackageName()))
                    continue outer;
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
    private void mergeBgFg(List<CustomUsageEvents> events) {
        List<DisplayEventEntity> copy = new ArrayList<>();
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

                    DisplayEventEntity event = new DisplayEventEntity(thisEvent.packageName,
                            nextEvent.timestamp, thisEvent.timestamp);
                    getIconName(event);
                    copy.add(event);
                    skip = true;
                } else if (i == 0) {
                    // Making sure that Ongoing events in the middle of the list get dropped
                    DisplayEventEntity event = new DisplayEventEntity(thisEvent.packageName,
                            nextEvent.timestamp, 1);
                    getIconName(event);
                    copy.add(event);
                }
            } else if (i == 0) {
                // Making sure that Ongoing events in the middle of the list get dropped
                DisplayEventEntity event = new DisplayEventEntity(thisEvent.packageName,
                        nextEvent.timestamp, 1);
                getIconName(event);
                copy.add(event);
            }
        }
        Log.i("GAAH2", "mergeBgFg: Size " + copy.size());
        displayLiveData.setValue(copy);
    }


    /*
    * Merges items from the same package name together if the events are less than MIN_TIME_DIFFERENCE ms apart
    */
    private void mergeSame() {
        final long MIN_TIME_DIFFERENCE = 1000 * 5;

        DisplayEventEntity previous = null;
        List<DisplayEventEntity> events = displayLiveData.getValue();

        if (events == null)
            return;

        Iterator<DisplayEventEntity> iterator = events.iterator();
        while (iterator.hasNext()) {
            if (previous == null) {
                previous = iterator.next();
                continue;
            }

            DisplayEventEntity thisEvent = iterator.next();

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
    }

    private void getIconName(DisplayEventEntity event) {
        try {
            event.appIcon = getApplication().getPackageManager()
                    .getApplicationIcon(event.packageName);
            event.appName = getAppName(event.packageName);
        } catch (PackageManager.NameNotFoundException e) {
            event.appIcon = getApplication()
                    .getDrawable(R.drawable.ic_default_app_launcher);
        }
    }

    private String getAppName(String packageName) {
        ApplicationInfo applicationInfo;
        PackageManager packageManager = getApplication().getPackageManager();
        try {
            applicationInfo = packageManager
                    .getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
        if (applicationInfo != null)
            return packageManager.getApplicationLabel(applicationInfo).toString();
        else
            return packageName;
    }

    public LiveData<List<DisplayEventEntity>> setDisplayUsageEventsList(UsageStatsManager mUsageStatsManager,
                                                                        String[] excludePackages) {
        List<CustomUsageEvents> usageEvents = getUsageEvents(mUsageStatsManager, excludePackages);
        if (usageEvents == null)
            return null;

        if (db == null)
            db = Room.databaseBuilder(getApplication(), Database.class, "eventsDb").build();

        Collections.sort(usageEvents, (left, right) ->
                Long.compare(right.timestamp, left.timestamp));

        mergeBgFg(usageEvents);
        mergeSame();
        insertInDb();
        return displayLiveData;
    }

    public LiveData<List<DisplayEventEntity>> getDisplayUsageEventsList() {
        return displayLiveData;
    }

    private void insertInDb() {
        new Thread(() -> db.dao().insertEvent(displayLiveData.getValue())).start();
    }
}
