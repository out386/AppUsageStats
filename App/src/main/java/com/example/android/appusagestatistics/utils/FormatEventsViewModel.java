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
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.appusagestatistics.R;
import com.example.android.appusagestatistics.database.Database;
import com.example.android.appusagestatistics.models.CustomUsageEvents;
import com.example.android.appusagestatistics.models.DisplayEventEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by j on 4/7/17.
 */

public class FormatEventsViewModel extends AndroidViewModel {

    private MutableLiveData<List<DisplayEventEntity>> displayLiveData = new MutableLiveData<>();
    private Database db;

    public FormatEventsViewModel(Application application) {
        super(application);
    }

    /**
     * Returns the list including the time span specified by the
     * intervalType argument.
     *
     * @return A list of {@link android.app.usage.UsageStats}.
     */
    private List<CustomUsageEvents> getUsageEvents(UsageStatsManager mUsageStatsManager,
                                                   String[] excludePackages, long startTime, long endTime) {
        List<CustomUsageEvents> copy = new ArrayList<>();
        UsageEvents.Event event = new UsageEvents.Event();
        String eventType;
        UsageEvents queryUsageEvents = mUsageStatsManager.queryEvents(startTime, endTime);

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
    private List<DisplayEventEntity> mergeBgFg(List<CustomUsageEvents> events) {
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
        return copy;
    }


    /*
    * Merges items from the same package name together if the events are less than MIN_TIME_DIFFERENCE ms apart
    */
    private List<DisplayEventEntity> mergeSame(List<DisplayEventEntity> events) {
        final long MIN_TIME_DIFFERENCE = 1000 * 5;

        DisplayEventEntity previous = null;

        if (events == null)
            return null;

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
        return events;
    }

    private void getIconName(DisplayEventEntity event) {
        try {
            event.appIcon = getApplication().getPackageManager()
                    .getApplicationIcon(event.packageName);
        } catch (PackageManager.NameNotFoundException e) {
            event.appIcon = getApplication()
                    .getDrawable(R.drawable.ic_default_app_launcher);
        }
        event.appName = getAppName(event.packageName);
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
        if (applicationInfo != null) {
            String name = packageManager.getApplicationLabel(applicationInfo).toString();
            return "".equals(name) ? packageName : name;
        } else
            return packageName;
    }

    public LiveData<List<DisplayEventEntity>> getDisplayUsageEventsList() {
        return displayLiveData;
    }

    private void insertInDb(List<DisplayEventEntity> events) {
        if (db == null)
            return;
        new Thread(() -> db.dao().insertEvent(events)).start();
    }

    private void findEvents(UsageStatsManager usageStatsManager,
                            String[] excludePackages, long startTime, long endTime,
                            List<DisplayEventEntity> oldEntities, final int NUMBER_TO_REMOVE) {

        List<CustomUsageEvents> usageEvents = getUsageEvents(usageStatsManager, excludePackages, startTime, endTime);
        if (usageEvents == null)
            return;

        Collections.sort(usageEvents, (left, right) ->
                Long.compare(right.timestamp, left.timestamp));

        List<DisplayEventEntity> merged = mergeBgFg(usageEvents);
        merged = mergeSame(merged);

        if (oldEntities != null) {
            removeUnstables(oldEntities, NUMBER_TO_REMOVE, merged);
        } else {
            displayLiveData.setValue(merged);
            insertInDb(merged);
        }
    }

    private void removeUnstables(List<DisplayEventEntity> eventsInDb, int numberToRemove, List<DisplayEventEntity> merged) {
        if (eventsInDb.size() < numberToRemove)
            numberToRemove = eventsInDb.size();

        int finalNumberToRemove = numberToRemove;
        AsyncTask<Void, Void, Void> remove = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Iterator<DisplayEventEntity> iterator = eventsInDb.iterator();
                for (int i = 0; i < finalNumberToRemove && iterator.hasNext(); i++) {
                    DisplayEventEntity current = iterator.next();
                    db.dao().deleteEvent(current);
                    Log.i("removed", "removeUnstables: deleted " + current.appName + current.startTime);
                    iterator.remove();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                for (int i = merged.size() - 1; i >= 0; i--) {
                    eventsInDb.add(0, merged.get(i));
                    Log.d("merge", "onPostExecute: number: " + i + " merged: " + merged.get(i).appName);
                }
                displayLiveData.setValue(eventsInDb);
                insertInDb(merged);
                super.onPostExecute(aVoid);
            }
        };

        remove.execute();
    }

    public void setDisplayUsageEventsList(UsageStatsManager usageStatsManager,
                                          String[] excludePackages, long startTime, long endTime) {

        if (db == null)
            db = Room.databaseBuilder(getApplication(), Database.class, "eventsDb").build();

        AsyncTask<Void, Void, List<DisplayEventEntity>> populateList = new AsyncTask<Void, Void, List<DisplayEventEntity>>() {
            private final int NUMBER_TO_REMOVE = 3;

            @Override
            protected List<DisplayEventEntity> doInBackground(Void... voids) {
                return db.dao().getEvents(startTime, endTime);
            }

            @Override
            protected void onPostExecute(List<DisplayEventEntity> eventsInDb) {
                if (eventsInDb == null || eventsInDb.size() == 0) {
                    Log.d("GAAH", "setDisplayUsageEventsList: null " + startTime + " " + endTime);
                    findEvents(usageStatsManager, excludePackages, startTime, endTime, null, -1);
                } else {
                    for (int i = 0; i < 10; i++) {
                        Log.i("database", "onPostExecute: " + eventsInDb.get(i).appName + " " + eventsInDb.get(i).startTime);
                    }

                    long newStartTime = eventsInDb.get(NUMBER_TO_REMOVE).endTime + 20;

                    Log.i("GAAH", "onPostExecute: new start time " + newStartTime);
                    findEvents(usageStatsManager, excludePackages, newStartTime, endTime,
                            eventsInDb, NUMBER_TO_REMOVE);
                }
                super.onPostExecute(eventsInDb);
            }
        };

        populateList.execute();
    }
}
