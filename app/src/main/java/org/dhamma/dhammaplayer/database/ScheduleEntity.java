package org.dhamma.dhammaplayer.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "schedule")
public class ScheduleEntity {
    @PrimaryKey(autoGenerate = true)
    private long mKey;

    // Schedule label.
    @ColumnInfo(name = "label")
    private String mLabel;

    // Hour of the day for alarm.
    @ColumnInfo(name = "hour")
    private int mHour;

    // Minute of the hour for alarm.
    @ColumnInfo(name = "minute")
    private int mMinute;

    // Days to repeat the alarm.
    @ColumnInfo(name = "days")
    private int mDays;

    // Type of the media files (audio / video)
    @ColumnInfo(name = "media_type")
    private String mMediaType;

    // Alarm active status.
    @ColumnInfo(name = "active")
    private boolean mActiveStatus;

    // Total media files count for this schedule.
    @ColumnInfo(name = "media_count")
    private int mMediaCount;

    // Last played media file index for this schedule.
    @ColumnInfo(name = "last_played")
    private int mLastPlayed;

    public ScheduleEntity(String label, int hour, int minute, int days, String mediaType, int mediaCount) {
        mLabel = label;
        mHour = hour;
        mMinute = minute;
        mDays = days;
        mActiveStatus = true;
        mMediaType = mediaType;
        mMediaCount = mediaCount;
        mLastPlayed = -1;
    }

    public long getKey() {
        return mKey;
    }

    public String getLabel() {
        return mLabel;
    }

    public int getHour() {
        return mHour;
    }

    public int getMinute() {
        return mMinute;
    }

    public int getDays() {
        return mDays;
    }

    public String getMediaType() {
        return mMediaType;
    }

    public boolean getActiveStatus() {
        return mActiveStatus;
    }

    public int getMediaCount() {
        return mMediaCount;

    }
    public int getLastPlayed() {
        return mLastPlayed;
    }

    public void setKey(long key) {
        mKey = key;
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    public void setHour(int hour) {
        mHour = hour;
    }

    public void setMinute(int minute) {
        mMinute = minute;
    }

    public void setDays(int days) {
        mDays = days;
    }

    public void setMediaType(String mediaType) {
        mMediaType = mediaType;
    }

    public void setActiveStatus(boolean activeStatus) {
        mActiveStatus = activeStatus;
    }

    public void setMediaCount(int mediaCount) {
        mMediaCount = mediaCount;
    }

    public void setLastPlayed(int lastPlayed) {
        mLastPlayed = lastPlayed;
    }
}
