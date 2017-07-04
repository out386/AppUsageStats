/*
* Copyright 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.appusagestatistics.fragments;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.appusagestatistics.models.DisplayUsageEvents;
import com.example.android.appusagestatistics.utils.Constants;
import com.example.android.appusagestatistics.utils.FormatCustomUsageEvents;
import com.example.android.appusagestatistics.utils.comparators.TimestampComparator;
import com.example.android.appusagestatistics.models.CustomUsageEvents;
import com.example.android.appusagestatistics.R;
import com.example.android.appusagestatistics.adapters.UsageListAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import java.util.List;

/**
 * Fragment that demonstrates how to use App Usage Statistics API.
 */
public class AppUsageStatisticsFragment extends Fragment {

    private static final String TAG = AppUsageStatisticsFragment.class.getSimpleName();

    //VisibleForTesting for variables below
    UsageStatsManager mUsageStatsManager;
    UsageListAdapter mUsageListAdapter;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    Button mOpenUsageSettingButton;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment {@link AppUsageStatisticsFragment}.
     */
    public static AppUsageStatisticsFragment newInstance() {
        AppUsageStatisticsFragment fragment = new AppUsageStatisticsFragment();
        return fragment;
    }

    public AppUsageStatisticsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUsageStatsManager = (UsageStatsManager) getActivity()
                .getSystemService(Context.USAGE_STATS_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_usage_statistics, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        mUsageListAdapter = new UsageListAdapter(getContext());
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_app_usage);
        mLayoutManager = mRecyclerView.getLayoutManager();
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.setAdapter(mUsageListAdapter);
        mOpenUsageSettingButton = (Button) rootView.findViewById(R.id.button_open_usage_setting);


    }

    @Override
    public void onResume() {
        super.onResume();
        List<UsageEvents.Event> usageStatsList =
                getUsageEvents();

        List<CustomUsageEvents> copy = new ArrayList<>();
        for (int i = 0; i < usageStatsList.size(); i++) {
            String eventType = null;

            if (usageStatsList.get(i).getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                eventType = Constants.FG;
                copy.add(new CustomUsageEvents(usageStatsList.get(i).getPackageName(),
                        eventType, usageStatsList.get(i).getTimeStamp()));
            }
            else if (usageStatsList.get(i).getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                eventType = Constants.BG;
                copy.add(new CustomUsageEvents(usageStatsList.get(i).getPackageName(),
                        eventType, usageStatsList.get(i).getTimeStamp()));
            }
        }

        Collections.sort(copy, new TimestampComparator());
        //copy = FormatCustomUsageEvents.removeOld(copy);
        updateAppsList(FormatCustomUsageEvents.mergeBgFg(copy));
    }

    /**
     * Returns the {@link #mRecyclerView} including the time span specified by the
     * intervalType argument.
     *
     * @return A list of {@link android.app.usage.UsageStats}.
     */
    public List<UsageEvents.Event> getUsageEvents() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);

        UsageEvents queryUsageEvents = mUsageStatsManager
                .queryEvents(cal.getTimeInMillis(),
                        System.currentTimeMillis());

        if (!queryUsageEvents.hasNextEvent()) {
            Log.i(TAG, "The user may not allow the access to apps usage. ");
            Toast.makeText(getActivity(),
                    getString(R.string.explanation_access_to_appusage_is_not_enabled),
                    Toast.LENGTH_LONG).show();
            mOpenUsageSettingButton.setVisibility(View.VISIBLE);
            mOpenUsageSettingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                }
            });
        }

        UsageEvents.Event event = new UsageEvents.Event();
        List<UsageEvents.Event> events = new ArrayList<>();
        while (queryUsageEvents.getNextEvent(event)) {
            events.add(event);
            event = new UsageEvents.Event();
        }

        return events;
    }

    /**
     * Updates the {@link #mRecyclerView} with the list of {@link UsageStats} passed as an argument.
     *
     * @param usageStatsList A list of {@link UsageStats} from which update the
     *                       {@link #mRecyclerView}.
     */
    //VisibleForTesting
    void updateAppsList(List<DisplayUsageEvents> usageStatsList) {
        for (int i = 0; i < usageStatsList.size(); i++) {
            try {
                usageStatsList.get(i).appIcon = getActivity().getPackageManager()
                        .getApplicationIcon(usageStatsList.get(i).packageName);
                usageStatsList.get(i).appName = getAppName(usageStatsList.get(i).packageName);
            } catch (PackageManager.NameNotFoundException e) {
                usageStatsList.get(i).appIcon = getActivity()
                        .getDrawable(R.drawable.ic_default_app_launcher);
            }
        }
        mUsageListAdapter.setCustomUsageStatsList(usageStatsList);
        mUsageListAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);
    }

    private String getAppName(String packageName) {
        ApplicationInfo applicationInfo;
        PackageManager packageManager = getActivity().getPackageManager();
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
}
