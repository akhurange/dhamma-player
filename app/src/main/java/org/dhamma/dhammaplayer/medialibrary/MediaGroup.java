package org.dhamma.dhammaplayer.medialibrary;

import org.dhamma.dhammaplayer.database.MediaFileEntity;
import org.dhamma.dhammaplayer.database.ScheduleEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import androidx.core.util.Pair;

public class MediaGroup {
    private long mScheduleId;
    private String mTimeStamp;
    private String mScheduleLabel;
    private String mMediaType;
    private static Semaphore sLock = new Semaphore(1);
    private static Map<Long, ArrayList<Pair<String ,String>>> sMediaFilesMap = new HashMap<>();

    public static void buildMediaFilesMap(List<MediaFileEntity> mediaFileEntities) {
        try {
            sLock.acquire();
            sMediaFilesMap.clear();
            for (MediaFileEntity mediaFileEntity : mediaFileEntities) {
                Long scheduleId = mediaFileEntity.getScheduleId();
                if (sMediaFilesMap.containsKey(scheduleId)) {
                    sMediaFilesMap.get(scheduleId).add(new Pair <String, String>(mediaFileEntity.getMediaPath(), mediaFileEntity.getMediaTitle()));
                } else {
                    ArrayList<Pair<String,String>> mediaList = new ArrayList<>();
                    mediaList.add(new Pair<String, String>(mediaFileEntity.getMediaPath(), mediaFileEntity.getMediaTitle()));
                    sMediaFilesMap.put(scheduleId, mediaList);
                }
            }
            sLock.release();
        } catch (InterruptedException e) {
            // TODO
        }
    }

    public MediaGroup(ScheduleEntity scheduleEntity) {
        int hour = scheduleEntity.getHour();
        int minute = scheduleEntity.getMinute();
        mScheduleId = scheduleEntity.getKey();
        mScheduleLabel = scheduleEntity.getLabel();
        mMediaType = scheduleEntity.getMediaType();
        String AM_PM;
        if (hour < 12) {
            AM_PM = "AM";
        } else {
            AM_PM = "PM";
            hour -= 12;
            if (0 == hour) {
                hour = 12;
            }
        }
        mTimeStamp = hour+":"+String.format("%02d",minute)+" "+AM_PM;
    }

    public String getTimeStamp() {
        return mTimeStamp;
    }

    public String getScheduleLabel() {
        return mScheduleLabel;
    }

    public String getMediaType() {
        return mMediaType;
    }

    public ArrayList<Pair<String, String>> getMediaList() {
        ArrayList<Pair<String, String>> mediaList = null;
        try {
            sLock.acquire();
            mediaList = sMediaFilesMap.get(mScheduleId);
            sLock.release();
        } catch (InterruptedException e) {
            // TODO
        }
        return mediaList;
    }
}
