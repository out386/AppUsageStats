package com.example.android.appusagestatistics.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by j on 23/7/17.
 */

public class AppFilteredEvents {
    public List<DisplayEventEntity> appEvents = new ArrayList<>();
    public List<DisplayEventEntity> otherEvents = new ArrayList<>();
    public long startTime;
    public long endTime;
}
