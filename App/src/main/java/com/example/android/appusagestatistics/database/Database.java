package com.example.android.appusagestatistics.database;

import android.arch.persistence.room.RoomDatabase;

import com.example.android.appusagestatistics.models.DisplayEventEntity;

/**
 * Created by j on 11/7/17.
 */
@android.arch.persistence.room.Database(entities = DisplayEventEntity.class, version = 1)
public abstract class Database extends RoomDatabase{
    public abstract Dao dao();
}
