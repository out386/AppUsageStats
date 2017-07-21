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

import android.animation.Animator;
import android.app.AppOpsManager;
import android.app.usage.UsageStatsManager;
import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.android.appusagestatistics.R;
import com.example.android.appusagestatistics.adapters.ScrollAdapter;
import com.example.android.appusagestatistics.models.DisplayEventEntity;
import com.example.android.appusagestatistics.recycler.TotalItem;
import com.example.android.appusagestatistics.utils.DisplaySize;
import com.example.android.appusagestatistics.utils.FormatEventsViewModel;
import com.example.android.appusagestatistics.utils.Tools;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.GenericItemAdapter;
import com.turingtechnologies.materialscrollbar.DateAndTimeIndicator;
import com.turingtechnologies.materialscrollbar.TouchScrollBar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;

public class AppUsageStatisticsFragment extends LifecycleFragment {

    private static final String TAG = AppUsageStatisticsFragment.class.getSimpleName();
    @BindView(R.id.recyclerview_app_usage)
    RecyclerView mRecyclerView;
    @BindView(R.id.header_usage)
    TextView headerUsage;
    @BindArray(R.array.exclude_packages)
    String[] excludePackages;
    @BindView(R.id.date_next)
    Button dateNext;
    @BindView(R.id.date_previous)
    Button datePrev;
    @BindView(R.id.date_text)
    TextView dateText;
    @BindView(R.id.date_layout)
    RelativeLayout dateLayout;
    @BindView(R.id.floating_date_text)
    TextView floatingDate;
    private UsageStatsManager mUsageStatsManager;
    private Unbinder unbinder;
    private FormatEventsViewModel formatCustomUsageEvents;
    private MaterialDialog dialog;
    private int mDateOffset = 0;
    private boolean isJustNoOffset = false;
    private boolean isDateLayoutVisible = true;
    private SimpleDateFormat sdf = new SimpleDateFormat("d MMMM");
    private PackageManager pm;
    private Handler mFloatingDateHandler;
    private Runnable mFloatingDateRunnable;


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
        pm = getActivity().getPackageManager();
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
        if (mRecyclerView != null)
            mRecyclerView.clearOnScrollListeners();
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        mRecyclerView.scrollToPosition(0);

        FastAdapter<TotalItem> mFastAdapter = new FastAdapter<>();
        GenericItemAdapter<DisplayEventEntity, TotalItem> mTotalAdapter =
                new GenericItemAdapter<>(TotalItem.class, DisplayEventEntity.class);
        ScrollAdapter scrollAdapter = new ScrollAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        mRecyclerView.setItemAnimator(null);
        mFastAdapter.withSelectable(true);

        mRecyclerView.setAdapter(scrollAdapter.wrap(mTotalAdapter.wrap(mFastAdapter)));

        TouchScrollBar materialScrollBar = new TouchScrollBar(getActivity().getApplicationContext(),
                mRecyclerView, true);
        materialScrollBar.setHandleColourRes(R.color.colorAccent);
        materialScrollBar.setBarColourRes(R.color.scrollbarBgGray);
        materialScrollBar.addIndicator(new DateAndTimeIndicator(getActivity().
                getApplicationContext(), false, false, false, true), true);


        formatCustomUsageEvents = ViewModelProviders
                .of(this)
                .get(FormatEventsViewModel.class);

        formatCustomUsageEvents
                .getDisplayUsageEventsList()
                .observe(this, events -> {
                    if (!checkForPermission()) {
                        Log.i(TAG, "The user may not have allowed access to apps usage.");
                        dialog = showDialog();
                    } else {
                        if (events == null || events.size() == 0) {
                            mTotalAdapter.clear();
                            headerUsage.setText(getResources().getString(R.string.no_usage));
                            return;
                        }
                        String formattedTime = Tools.formatTotalTime(0, findTotalUsage(events), false);
                        headerUsage.setText(String.format(getResources().getString(R.string.total_usage),
                                formattedTime == null ?
                                        getResources().getString(R.string.no_usage) : formattedTime));

                        if (!isJustNoOffset && mDateOffset == 0 && mTotalAdapter.getItem(0) != null) {
                            int index = findItemInList(events, mTotalAdapter.getItem(1).getModel());
                            if (index > -1) {
                                mTotalAdapter.removeModel(0);
                            }
                            for (int i = index - 1; i >= 0; i--) {
                                mTotalAdapter.addModel(0, events.get(i));
                            }
                        } else {
                            isJustNoOffset = false;
                            Log.i(TAG, "onViewCreated: clearing");
                            mTotalAdapter.clear();
                            mTotalAdapter.addModel(events);
                        }
                    }
                });

        datePrev.setOnClickListener(view -> {
            mDateOffset -= 1;
            if (!dateNext.isEnabled()) {
                dateNext.setEnabled(true);
                dateNext.setTextColor(ContextCompat
                        .getColor(getActivity().getApplicationContext(), R.color.textWhite));
            }
            triggerEvents(true);
        });
        dateNext.setOnClickListener(view -> {
            mDateOffset += 1;
            if (mDateOffset > 0) {
                mDateOffset = 0;
                dateNext.setEnabled(false);
                dateNext.setTextColor(ContextCompat
                        .getColor(getActivity().getApplicationContext(), R.color.textDisabled));
            } else {
                if (mDateOffset == 0) {
                    isJustNoOffset = true;
                    dateNext.setEnabled(false);
                    dateNext.setTextColor(ContextCompat
                            .getColor(getActivity().getApplicationContext(), R.color.textDisabled));
                }
                triggerEvents(true);
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isDateLayoutVisible && dy > 2) {
                    isDateLayoutVisible = false;
                    dateLayout.animate().translationY(dateLayout.getHeight()).setDuration(250).start();
                } else if (!isDateLayoutVisible && dy < -2) {
                    isDateLayoutVisible = true;
                    dateLayout.animate().translationY(0).setDuration(250).start();
                }
                if (floatingDate != null && floatingDate.getVisibility() == View.VISIBLE && (dy < -10 || dy > 10)) {
                    if (mFloatingDateHandler != null && mFloatingDateRunnable != null)
                        mFloatingDateHandler.removeCallbacks(mFloatingDateRunnable);
                    hideFloatingDate();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        triggerEvents(false);
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

    private int findItemInList(List<DisplayEventEntity> list, DisplayEventEntity event) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).appName.equals(event.appName) && list.get(i).startTime == event.startTime)
                return i;
        }
        return -1;
    }

    private void triggerEvents(boolean isButtonClicked) {
        Calendar startCalender = Calendar.getInstance();
        if (mDateOffset < 0)
            startCalender.add(Calendar.DATE, mDateOffset);
        startCalender.set(Calendar.HOUR_OF_DAY, 0);
        startCalender.set(Calendar.MINUTE, 0);
        startCalender.set(Calendar.SECOND, 0);
        startCalender.set(Calendar.MILLISECOND, 0);


        Calendar endCalendar = Calendar.getInstance();
        if (mDateOffset < 0)
            endCalendar.add(Calendar.DATE, mDateOffset);
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);
        endCalendar.set(Calendar.MILLISECOND, 999);

        String formattedDate = sdf.format(new Date(startCalender.getTimeInMillis()));
        dateText.setText(formattedDate);

        if (isButtonClicked) {
            formattedDate = formattedDate.replace(" ", "\n");
            showHideFloatingDate(formattedDate);
        }

        formatCustomUsageEvents
                .setDisplayUsageEventsList(mUsageStatsManager, excludePackages,
                        startCalender.getTimeInMillis(), endCalendar.getTimeInMillis(), true,
                        mDateOffset < 0);
    }

    private boolean checkForPermission() {
        AppOpsManager appOps = (AppOpsManager) getActivity()
                .getApplicationContext()
                .getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo info;
        try {
            info = pm.getApplicationInfo(getActivity()
                    .getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, info.uid, getActivity()
                .getApplicationContext().getPackageName());
        return mode == MODE_ALLOWED;
    }

    private void showHideFloatingDate(String text) {
        if (floatingDate == null)
            return;
        floatingDate.setText(text);

        floatingDate.setAlpha(0.0F);
        floatingDate.setVisibility(View.VISIBLE);
        floatingDate
                .animate()
                .alpha(1.0F)
                .setDuration(250)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        if (mFloatingDateHandler == null)
                            mFloatingDateHandler = new Handler();

                        if (mFloatingDateRunnable == null) {
                            mFloatingDateRunnable = () -> hideFloatingDate();
                        }
                        mFloatingDateHandler.removeCallbacks(mFloatingDateRunnable);
                        mFloatingDateHandler.postDelayed(mFloatingDateRunnable, 1000);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                })
                .start();
    }

    private void hideFloatingDate() {
        if (floatingDate != null) {
            if (floatingDate.getVisibility() == View.GONE)
                return;
            floatingDate
                    .animate()
                    .alpha(0.0F)
                    .setDuration(250)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            floatingDate.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
        }
    }
}
