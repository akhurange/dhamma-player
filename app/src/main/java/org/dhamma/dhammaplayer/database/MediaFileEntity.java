package org.dhamma.dhammaplayer.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "media_files")
public class MediaFileEntity {
    @PrimaryKey(autoGenerate = true)
    private long mKey;

    // primary key of the schedule
    @ColumnInfo(name = "schedule_id")
    private long mScheduleId;

    // Full Path of media file.
    @ColumnInfo(name = "media_path")
    private String mMediaPath;

    // Full Path of media file.
    @ColumnInfo(name = "media_title")
    private String mMediaTitle;

    public MediaFileEntity(long scheduleId, String mediaPath, String mediaTitle) {
        mScheduleId = scheduleId;
        mMediaPath = mediaPath;
        mMediaTitle = mediaTitle;
    }

    public long getKey() {
        return mKey;
    }

    public long getScheduleId() {
        return mScheduleId;
    }

    public String getMediaPath() {
        return mMediaPath;
    }

    public String getMediaTitle() {
        return mMediaTitle;
    }

    public void setScheduleId(long scheduleId) {
        mScheduleId = scheduleId;
    }

    public void setKey(long key) {
        mKey = key;
    }

    public void setMediaPath(String mediaPath) {
        mMediaPath = mediaPath;
    }

    public void setMediaTitle(String mediaTitle) {
        mMediaTitle = mediaTitle;
    }
}
