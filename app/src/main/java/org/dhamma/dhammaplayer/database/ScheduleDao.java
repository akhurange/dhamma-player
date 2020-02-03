package org.dhamma.dhammaplayer.database;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long insertSchedule(ScheduleEntity scheduleEntity);

    @Update
    public void updateSchedule(ScheduleEntity scheduleEntity);

    @Delete
    public void deleteSchedule(ScheduleEntity scheduleEntity);

    @Query("SELECT * FROM schedule")
    public LiveData<List<ScheduleEntity>> liveLoadSchedules();
}
