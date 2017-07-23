package com.example.android.appusagestatistics.database;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.example.android.appusagestatistics.models.DisplayEventEntity;

import java.util.List;

/**
 * Created by j on 11/7/17.
 */
@android.arch.persistence.room.Dao
public interface Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEvent(List<DisplayEventEntity> event);

    @Query("SELECT * FROM events WHERE startTime >= :startTime AND endTime <= :endTime AND "
            + "endTime <> 0 ORDER BY startTime DESC")
    List<DisplayEventEntity> getEvents(long startTime, long endTime);

    @Query("SELECT * FROM events WHERE startTime >= :startTime AND endTime <= :endTime AND "
            + "endTime <> 0 AND appName = :appName ORDER BY startTime DESC")
    List<DisplayEventEntity> getDetailEvents(long startTime, long endTime, String appName);

    @Delete
    void deleteEvent(DisplayEventEntity unstable);
}
