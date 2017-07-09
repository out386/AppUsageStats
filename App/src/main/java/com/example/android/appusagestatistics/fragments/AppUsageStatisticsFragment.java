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
import android.os.Build;
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

import com.example.android.appusagestatistics.R;
import com.example.android.appusagestatistics.adapters.UsageListAdapter;
import com.example.android.appusagestatistics.models.DisplayUsageEvents;
import com.example.android.appusagestatistics.utils.FormatCustomUsageEvents;

import java.util.List;

public class AppUsageStatisticsFragment extends Fragment {

    private static final String TAG = AppUsageStatisticsFragment.class.getSimpleName();

    private UsageStatsManager mUsageStatsManager;
    private UsageListAdapter mUsageListAdapter;
    private RecyclerView mRecyclerView;
    private Button mOpenUsageSettingButton;
    private CheckBox mHideLauncherCheckBox;
    private List<DisplayUsageEvents> events;

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
        return inflater.inflate(R.layout.fragment_app_usage_statistics, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        mUsageListAdapter = new UsageListAdapter(getContext());
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_app_usage);
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.setAdapter(mUsageListAdapter);
        mOpenUsageSettingButton = (Button) rootView.findViewById(R.id.button_open_usage_setting);


    }

    @Override
    public void onResume() {
        super.onResume();

        List<DisplayUsageEvents> events = FormatCustomUsageEvents.getDisplayUsageEventsList(mUsageStatsManager);
        if (events == null) {
            Log.i(TAG, "The user may not have allowed access to apps usage.");
            Toast.makeText(getActivity(),
                    getString(R.string.explanation_access_to_appusage_is_not_enabled),
                    Toast.LENGTH_LONG).show();
            mOpenUsageSettingButton.setVisibility(View.VISIBLE);
            mOpenUsageSettingButton.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
        } else {
            mOpenUsageSettingButton.setVisibility(View.GONE);
            updateAppsList(events);
        }
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
