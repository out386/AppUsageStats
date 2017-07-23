package com.example.android.appusagestatistics.fragments;


import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.appusagestatistics.R;
import com.example.android.appusagestatistics.adapters.ScrollAdapter;
import com.example.android.appusagestatistics.models.AppFilteredEvents;
import com.example.android.appusagestatistics.models.DisplayEventEntity;
import com.example.android.appusagestatistics.recycler.TotalItem;
import com.example.android.appusagestatistics.utils.FormatEventsViewModel;
import com.example.android.appusagestatistics.utils.Tools;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.GenericItemAdapter;
import com.turingtechnologies.materialscrollbar.DateAndTimeIndicator;
import com.turingtechnologies.materialscrollbar.TouchScrollBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.PieChartView;

public class AppDetailFragment extends LifecycleFragment {

    private static final String KEY_APP_NAME = "appName";
    private static final String KEY_DATE_OFFSET = "dateOffset";

    @BindView(R.id.recyclerview_app_detail)
    RecyclerView mRecyclerView;
    @BindView(R.id.detail_chart)
    PieChartView chart;
    @BindView(R.id.detail_name)
    TextView detailName;
    @BindView(R.id.detail_usage)
    TextView detailUsage;

    private Unbinder mUnbinder;
    private FormatEventsViewModel formatCustomUsageEvents;
    private String mAppName;
    private int mDateOffset;
    private PieChartData data;

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

        detailName.setText(mAppName);


        formatCustomUsageEvents = ViewModelProviders
                .of(this)
                .get(FormatEventsViewModel.class);

        formatCustomUsageEvents
                .getAppDetailEventsList()
                .observe(this, allEvents -> {
                    AppFilteredEvents appFilteredEvents = Tools.getSpecificAppEvents(allEvents, mAppName);
                    if (appFilteredEvents.appEvents == null || appFilteredEvents.appEvents.size() == 0) {
                        mTotalAdapter.clear();
                        detailUsage.setText(String.format(getResources().getString(R.string.total_usage),
                                        getResources().getString(R.string.no_usage)));
                        return;
                    }

                    long totalUsage = Tools.findTotalUsage(appFilteredEvents.appEvents);
                    String formattedTime = Tools.formatTotalTime(0, totalUsage, false);
                    detailUsage.setText(String.format(getResources().getString(R.string.total_usage),
                            formattedTime == null ?
                                    getResources().getString(R.string.no_usage) : formattedTime));

                    setPie(appFilteredEvents);

                    if (mTotalAdapter.getItem(0) != null) {
                        int index = findItemInList(appFilteredEvents.appEvents, mTotalAdapter.getItem(1).getModel());
                        if (index > -1) {
                            mTotalAdapter.removeModel(0);
                        }
                        for (int i = index - 1; i >= 0; i--) {
                            mTotalAdapter.addModel(0, appFilteredEvents.appEvents.get(i));
                        }
                    } else {
                        mTotalAdapter.clear();
                        mTotalAdapter.addModel(appFilteredEvents.appEvents);
                    }
                });

        triggerEvents();
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
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
                .setCachedEventsList(startCalender.getTimeInMillis(),
                        endCalendar.getTimeInMillis());
    }

    private void setPie(AppFilteredEvents appFilteredEvents) {
        final double TIME_DAY = 86399;
        final double TIME_USED_OTHERS = Tools.findTotalUsage(appFilteredEvents.otherEvents) / 1000;
        final double TIME_USED_THIS = Tools.findTotalUsage(appFilteredEvents.appEvents) / 1000;

        float otherPercent = (float) (TIME_USED_OTHERS / TIME_DAY * 100);
        float dayRemainingPercent = (float) ((TIME_DAY - TIME_USED_OTHERS - TIME_USED_THIS) / TIME_DAY * 100);
        float thisPercent = (float) (TIME_USED_THIS / TIME_DAY * 100);

        Log.i("huh", "generateData: " + otherPercent);
        Log.i("huh", "generateData: " + dayRemainingPercent);
        Log.i("huh", "generateData: " + thisPercent);
        Log.i("huh", "generateData: " + (otherPercent + thisPercent + dayRemainingPercent));

        List<SliceValue> values = new ArrayList<>();

        SliceValue sliceValue = new SliceValue(otherPercent, ChartUtils.pickColor());
        sliceValue.setLabel(getResources().getString(R.string.detail_other_apps));
        values.add(sliceValue);

        sliceValue = new SliceValue(dayRemainingPercent , ChartUtils.pickColor());
        sliceValue.setLabel(getResources().getString(R.string.detail_unused));
        values.add(sliceValue);

        sliceValue = new SliceValue(thisPercent , ChartUtils.pickColor());
        sliceValue.setLabel(getResources().getString(R.string.detail_this_app));
        values.add(sliceValue);


        data = new PieChartData(values);
        data.setHasLabels(true);
        data.setHasLabelsOutside(true);
        data.setHasCenterCircle(true);
        chart.offsetLeftAndRight(50);

        chart.setChartRotationEnabled(false);
        chart.setValueSelectionEnabled(false);
        chart.setCircleFillRatio(0.7f);
        chart.setPieChartData(data);
        chart.setVisibility(View.VISIBLE);
    }
}
