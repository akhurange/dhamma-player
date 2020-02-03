package org.dhamma.dhammaplayer.database;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface MediaFileDao {
    @Insert
    public void insertMediaFiles(MediaFileEntity... mediaFileEntity);

    @Query("SELECT * FROM media_files")
    public LiveData<List<MediaFileEntity>> liveLoadMediaFiles();

    @Query("SELECT * FROM media_files WHERE schedule_id = :scheduleId")
    public List<MediaFileEntity> loadMediaFilesForSchedule(long scheduleId);

    @Query("DELETE FROM media_files WHERE schedule_id = :scheduleId")
    public void deleteMediaFilesForSchedule(long scheduleId);

}
