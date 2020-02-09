package org.dhamma.dhammaplayer.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "media_files")
public class MediaFileEntity implements Comparable{
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

    // Index of this media in the schedule.
    @ColumnInfo(name = "schedule_index")
    private int mScheduleIndex;

    public MediaFileEntity(long scheduleId, String mediaPath, String mediaTitle, int scheduleIndex) {
        mScheduleId = scheduleId;
        mMediaPath = mediaPath;
        mMediaTitle = mediaTitle;
        mScheduleIndex = scheduleIndex;
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

    public int getScheduleIndex() {
        return mScheduleIndex;
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

    public void setScheduleIndex(int scheduleIndex) {
        mScheduleIndex = scheduleIndex;
    }

    @Override
    public int compareTo(Object o) {
        return this.mScheduleIndex - ((MediaFileEntity)o).getScheduleIndex();
    }
}
