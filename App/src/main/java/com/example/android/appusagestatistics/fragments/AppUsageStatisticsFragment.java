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

import android.app.usage.UsageStatsManager;
import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.android.appusagestatistics.R;
import com.example.android.appusagestatistics.adapters.UsageListAdapter;
import com.example.android.appusagestatistics.models.DisplayEventEntity;
import com.example.android.appusagestatistics.utils.DisplaySize;
import com.example.android.appusagestatistics.utils.FormatEventsViewModel;
import com.example.android.appusagestatistics.utils.Tools;

import java.util.Calendar;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AppUsageStatisticsFragment extends LifecycleFragment {

    private static final String TAG = AppUsageStatisticsFragment.class.getSimpleName();
    @BindView(R.id.recyclerview_app_usage)
    protected RecyclerView mRecyclerView;
    @BindView(R.id.header_time)
    protected TextView headerTime;
    @BindView(R.id.header_usage)
    protected TextView headerUsage;
    @BindArray(R.array.exclude_packages)
    protected String[] excludePackages;

    private UsageStatsManager mUsageStatsManager;
    private Unbinder unbinder;
    private FormatEventsViewModel formatCustomUsageEvents;
    private MaterialDialog dialog;
    private Handler handler;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment {@link AppUsageStatisticsFragment}.
     */
    public static AppUsageStatisticsFragment newInstance() {
        return new AppUsageStatisticsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT > 21)
            mUsageStatsManager = (UsageStatsManager) getActivity()
                    .getSystemService(Context.USAGE_STATS_SERVICE);
        else
            mUsageStatsManager = (UsageStatsManager) getActivity()
                    .getSystemService("usagestats");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_usage_statistics, container, false);
        unbinder = ButterKnife.bind(this, view);
        excludePackages[excludePackages.length - 1] = Tools.findLauncher(getActivity().getApplicationContext());
        return view;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        handler = new Handler();
        UsageListAdapter mUsageListAdapter = new UsageListAdapter(this);
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.setAdapter(mUsageListAdapter);
        formatCustomUsageEvents = ViewModelProviders
                .of(this)
                .get(FormatEventsViewModel.class);

        formatCustomUsageEvents
                .getDisplayUsageEventsList()
                .observe(this, events -> {
                    if (events == null) {
                        Log.i(TAG, "The user may not have allowed access to apps usage.");
                        dialog = showDialog();
                    } else {
                        String formattedTime = Tools.formatTotalTime(0, findTotalUsage(events), false);
                        headerUsage.setText(String.format(getResources().getString(R.string.total_usage),
                                formattedTime == null ?
                                        getResources().getString(R.string.no_usage) : formattedTime));
                    }
                });

        headerTime.setOnClickListener(view -> {
            mRecyclerView.scrollToPosition(30);
            handler.postDelayed(() ->
                    mRecyclerView.smoothScrollToPosition(0), 10);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Calendar cal = Calendar.getInstance();
        //cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.AM_PM, Calendar.AM);


        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DATE, -1);
        cal2.set(Calendar.HOUR, 23);
        cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 59);
        cal2.set(Calendar.MILLISECOND, 999);

        Log.i(TAG, "onResume: start time " + (cal.getTimeInMillis()));
        //Log.i(TAG, "onResume: end time " + (cal2.getTimeInMillis()));

        formatCustomUsageEvents
                .setDisplayUsageEventsList(mUsageStatsManager, excludePackages,
                        cal.getTimeInMillis(), System.currentTimeMillis(), true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dialog != null)
            dialog.dismiss();
    }

    private MaterialDialog showDialog() {
        MaterialDialog d = new MaterialDialog.Builder(getContext())
                .title(R.string.open_app_usage_setting_title)
                .content(R.string.explanation_access_to_appusage_is_not_enabled)
                .positiveText(R.string.ok)
                .canceledOnTouchOutside(false)
                .onPositive((dialog, which) -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)))
                .show();
        DisplaySize displaySize = Tools.getDisplaySizes(getActivity());
        Window w = d.getWindow();
        if (w != null) {
            w.setLayout(displaySize.width, w.getAttributes().height);
        }
        return d;
    }

    private long findTotalUsage(List<DisplayEventEntity> events) {
        long totalUsage = 0;
        for (DisplayEventEntity event : events) {
            if (event.endTime == 0)
                continue;
            totalUsage += event.endTime - event.startTime;
        }
        return totalUsage;
    }
}
