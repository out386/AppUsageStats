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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.appusagestatistics.R;
import com.example.android.appusagestatistics.models.CustomUsageEvents;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provide views to RecyclerView with the directory entries.
 */
public class UsageListAdapter extends RecyclerView.Adapter<UsageListAdapter.ViewHolder> {

    private List<CustomUsageEvents> mCustomUsageStatsList = new ArrayList<>();
    private DateFormat mDateFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mPackageName;
        private final TextView mEventTime;
        private final TextView mEventType;
        private final ImageView mAppIcon;

        ViewHolder(View v) {
            super(v);
            mPackageName = (TextView) v.findViewById(R.id.textview_package_name);
            mEventTime = (TextView) v.findViewById(R.id.event_time);
            mEventType = (TextView) v.findViewById(R.id.event_type);
            mAppIcon = (ImageView) v.findViewById(R.id.app_icon);
        }
    }

    public UsageListAdapter() {
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

        long lastTimeUsed = mCustomUsageStatsList.get(position).timestamp;
        String eventTypeString = mCustomUsageStatsList.get(position).eventType;

        viewHolder.mEventTime.setText(mDateFormat.format(new Date(lastTimeUsed)));
        viewHolder.mEventType.setText(eventTypeString);
        viewHolder.mAppIcon.setImageDrawable(mCustomUsageStatsList.get(position).appIcon);
    }

    @Override
    public int getItemCount() {
        return mCustomUsageStatsList.size();
    }

    public void setCustomUsageStatsList(List<CustomUsageEvents> customUsageStats) {
        mCustomUsageStatsList = customUsageStats;
    }
}