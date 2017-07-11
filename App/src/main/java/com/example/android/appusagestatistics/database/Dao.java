package com.example.android.appusagestatistics.database;

import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;

import com.example.android.appusagestatistics.models.DisplayEventEntity;

import java.util.List;

/**
 * Created by j on 11/7/17.
 */
@android.arch.persistence.room.Dao
public interface Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEvent(List<DisplayEventEntity> event);
}
