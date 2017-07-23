package com.example.android.appusagestatistics.fragments;


import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.appusagestatistics.R;
import com.example.android.appusagestatistics.adapters.ScrollAdapter;
import com.example.android.appusagestatistics.models.DisplayEventEntity;
import com.example.android.appusagestatistics.recycler.TotalItem;
import com.example.android.appusagestatistics.utils.FormatEventsViewModel;
import com.example.android.appusagestatistics.utils.Tools;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.GenericItemAdapter;
import com.turingtechnologies.materialscrollbar.DateAndTimeIndicator;
import com.turingtechnologies.materialscrollbar.TouchScrollBar;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AppDetailFragment extends LifecycleFragment {

    private static final String KEY_APP_NAME = "appName";
    private static final String KEY_DATE_OFFSET = "dateOffset";
    @BindView(R.id.recyclerview_app_detail)
    RecyclerView mRecyclerView;
    private Unbinder mUnbinder;
    private FormatEventsViewModel formatCustomUsageEvents;
    private String mAppName;
    private int mDateOffset;

    public static AppDetailFragment newInstance(String appName, int dateOffset) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_APP_NAME, appName);
        bundle.putInt(KEY_DATE_OFFSET, dateOffset);
        AppDetailFragment fragment = new AppDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mAppName = bundle.getString(KEY_APP_NAME);
            mDateOffset = bundle.getInt(KEY_DATE_OFFSET);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_app_detail, container, false);
        mUnbinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.scrollToPosition(0);

        FastAdapter<TotalItem> mFastAdapter = new FastAdapter<>();
        GenericItemAdapter<DisplayEventEntity, TotalItem> mTotalAdapter =
                new GenericItemAdapter<>(TotalItem.class, DisplayEventEntity.class);
        ScrollAdapter scrollAdapter = new ScrollAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        mRecyclerView.setItemAnimator(null);

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
                .getAppDetailEventsList()
                .observe(this, events -> {
                    if (events == null || events.size() == 0) {
                        mTotalAdapter.clear();
                        return;
                    }
                    String formattedTime = Tools.formatTotalTime(0, findTotalUsage(events), false);

                    if (mTotalAdapter.getItem(0) != null) {
                        int index = findItemInList(events, mTotalAdapter.getItem(1).getModel());
                        if (index > -1) {
                            mTotalAdapter.removeModel(0);
                        }
                        for (int i = index - 1; i >= 0; i--) {
                            mTotalAdapter.addModel(0, events.get(i));
                        }
                    } else {
                        mTotalAdapter.clear();
                        mTotalAdapter.addModel(events);
                    }
                });

        triggerEvents();
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
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

    private void triggerEvents() {
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

        formatCustomUsageEvents
                .setAppDetailEventsList(startCalender.getTimeInMillis(),
                        endCalendar.getTimeInMillis(), mAppName);
    }
}
