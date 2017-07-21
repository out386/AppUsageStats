package com.example.android.appusagestatistics.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.appusagestatistics.R;
import com.example.android.appusagestatistics.models.DisplayEventEntity;
import com.example.android.appusagestatistics.utils.Tools;
import com.mikepenz.fastadapter.items.GenericAbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by j on 15/7/17.
 */

public class TotalItem extends GenericAbstractItem<DisplayEventEntity, TotalItem, TotalItem.ViewHolder> {
    private static final ViewHolderFactory<? extends ViewHolder> FACTORY = new ItemFactory();
    private DateFormat mDateFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());

    public TotalItem(DisplayEventEntity displayEventEntity) {
        super(displayEventEntity);

    }
    
    @Override
    public int getType() {
        return R.id.start_time;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.usage_row;
    }

    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);
        
        viewHolder.mAppName.setText(getModel().appName);

        // Rounding off to the nearest second, as we aren't showing milliseconds
        long startTime = Math.round(getModel().startTime / 1000D) * 1000;
        long endTime = Math.round(getModel().endTime / 1000D) * 1000;

        viewHolder.mAppIcon.setImageDrawable(getModel().appIcon);
        viewHolder.mStartTime.setText(mDateFormat.format(new Date(startTime)));

        if (getModel().ongoing == 1) {
            viewHolder.mTotalTime.setText(viewHolder.mTotalTime.getContext()
                    .getResources().getString(R.string.ongoing));
            return;
        }

        String totalTime = Tools.formatTotalTime(startTime, endTime, true);
        viewHolder.mTotalTime.setText(totalTime == null ? viewHolder.mTotalTime.getContext()
                        .getResources().getString(R.string.no_usage) : totalTime);
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        /*holder.mTotalTime.setText(null);
        holder.mStartTime.setText(null);
        holder.mAppName.setText(null);
        if (holder.mAppIcon != null)
            holder.mAppIcon.setImageDrawable(null);*/
    }

    private static class ItemFactory implements ViewHolderFactory<ViewHolder> {
        public ViewHolder create(View v) {
            return new ViewHolder(v);
        }
    }
    @Override
    public ViewHolderFactory<? extends ViewHolder> getFactory() {
        return FACTORY;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.start_time)
        TextView mStartTime;
        @BindView(R.id.total_time)
        TextView mTotalTime;
        @BindView(R.id.textview_package_name)
        TextView mAppName;
        @BindView(R.id.app_icon)
        ImageView mAppIcon;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
