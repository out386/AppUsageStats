/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.appusagestatistics.adapters;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.appusagestatistics.R;
import com.example.android.appusagestatistics.fragments.AppUsageStatisticsFragment;
import com.example.android.appusagestatistics.models.DisplayEventEntity;
import com.example.android.appusagestatistics.utils.FormatEventsViewModel;
import com.example.android.appusagestatistics.utils.Tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Provide views to RecyclerView with the directory entries.
 */
public class UsageListAdapter extends RecyclerView.Adapter<UsageListAdapter.ViewHolder> {

    private List<DisplayEventEntity> mCustomUsageStatsList;
    private DateFormat mDateFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
    private Context mContext;

    public UsageListAdapter(AppUsageStatisticsFragment fragment) {
        mContext = fragment.getContext();
        FormatEventsViewModel formatCustomUsageEvents = ViewModelProviders
                .of(fragment)
                .get(FormatEventsViewModel.class);

        formatCustomUsageEvents
                .getDisplayUsageEventsList()
                .observe(fragment, events -> {
                    mCustomUsageStatsList = events;
                    notifyDataSetChanged();
                });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.usage_row, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.mAppName.setText(
                mCustomUsageStatsList.get(position).appName);

        // Rounding off to the nearest second, as we aren't showing milliseconds
        long startTime = Math.round(mCustomUsageStatsList.get(position).startTime / 1000D) * 1000;
        long endTime = Math.round(mCustomUsageStatsList.get(position).endTime / 1000D) * 1000;

        viewHolder.mAppIcon.setImageDrawable(mCustomUsageStatsList.get(position).appIcon);
        viewHolder.mStartTime.setText(mDateFormat.format(new Date(startTime)));
        if (mCustomUsageStatsList.get(position).ongoing == 1) {
            viewHolder.mTotalTime.setText(mContext.getResources().getString(R.string.ongoing));
            return;
        }
        viewHolder.mTotalTime.setText(Tools.formatTotalTime(startTime, endTime));
    }

    @Override
    public int getItemCount() {
        if (mCustomUsageStatsList == null)
            return 0;
        return mCustomUsageStatsList.size();
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textview_package_name)
        TextView mAppName;
        @BindView(R.id.start_time)
        TextView mStartTime;
        @BindView(R.id.total_time)
        TextView mTotalTime;
        @BindView(R.id.app_icon)
        ImageView mAppIcon;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}