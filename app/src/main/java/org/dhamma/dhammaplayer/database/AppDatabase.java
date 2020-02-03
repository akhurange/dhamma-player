package org.dhamma.dhammaplayer.database;

import android.content.Context;

import org.dhamma.dhammaplayer.media.MediaSelection;

import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ScheduleEntity.class, MediaFileEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase sInstance;

    @VisibleForTesting
    public static final String DATABASE_NAME = "org-dhamma-player.db";

    public abstract ScheduleDao scheduleDao();

    public abstract MediaFileDao mediaFileDao();

    public static AppDatabase getInstance(final Context context) {
        if (null == sInstance) {
            synchronized (AppDatabase.class) {
                if (null == sInstance) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DATABASE_NAME)
                            .build();
                }
            }
        }
        return sInstance;
    }

    public void dropDatabase() {
        clearAllTables();
    }
}