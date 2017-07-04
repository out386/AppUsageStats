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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.appusagestatistics.R;
import com.example.android.appusagestatistics.models.DisplayUsageEvents;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Provide views to RecyclerView with the directory entries.
 */
public class UsageListAdapter extends RecyclerView.Adapter<UsageListAdapter.ViewHolder> {

    private List<DisplayUsageEvents> mCustomUsageStatsList = new ArrayList<>();
    private DateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss");
    private DateFormat mDateFormatTotal = new SimpleDateFormat("HH:mm:ss");
    private Context mContext;

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mPackageName;
        private final TextView mStartTime;
        private final TextView mEndTime;
        private final TextView mTotalTime;
        private final ImageView mAppIcon;

        ViewHolder(View v) {
            super(v);
            mPackageName = (TextView) v.findViewById(R.id.textview_package_name);
            mStartTime = (TextView) v.findViewById(R.id.start_time);
            mEndTime = (TextView) v.findViewById(R.id.end_time);
            mTotalTime = (TextView) v.findViewById(R.id.total_time);
            mAppIcon = (ImageView) v.findViewById(R.id.app_icon);
        }
    }

    public UsageListAdapter(Context context) {
        mContext = context;
        mDateFormatTotal.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.usage_row, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.mPackageName.setText(
                mCustomUsageStatsList.get(position).appName);

        // Rounding off to the nearest second, as we aren't showing milliseconds
        long startTime = Math.round(mCustomUsageStatsList.get(position).startTime / 1000D) * 1000;
        long endTime = Math.round(mCustomUsageStatsList.get(position).endTime / 1000D) * 1000;
        boolean ongoing = mCustomUsageStatsList.get(position).ongoing;
        long totalTime = endTime - startTime;

        viewHolder.mAppIcon.setImageDrawable(mCustomUsageStatsList.get(position).appIcon);
        viewHolder.mStartTime.setText(mDateFormat.format(new Date(startTime)));
        if (ongoing) {
            viewHolder.mEndTime.setVisibility(View.GONE);
            viewHolder.mTotalTime.setText(mContext.getResources().getString(R.string.ongoing));
            return;
        }
        viewHolder.mEndTime.setText(mDateFormat.format(new Date(endTime)));
        viewHolder.mTotalTime.setText(mDateFormatTotal.format(new Date(totalTime)));
    }

    @Override
    public int getItemCount() {
        return mCustomUsageStatsList.size();
    }

    public void setCustomUsageStatsList(List<DisplayUsageEvents> customUsageStats) {
        mCustomUsageStatsList = customUsageStats;
    }
}