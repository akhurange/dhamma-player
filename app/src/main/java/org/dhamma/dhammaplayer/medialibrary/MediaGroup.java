package org.dhamma.dhammaplayer.medialibrary;

import org.dhamma.dhammaplayer.database.MediaFileEntity;
import org.dhamma.dhammaplayer.database.ScheduleEntity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class MediaGroup {
    private long mScheduleId;
    private String mTimeStamp;
    private String mScheduleLabel;
    private String mMediaType;
    private static Semaphore sLock = new Semaphore(1);
    private static Map<Long, ArrayList<MediaFileEntity>> sMediaFilesMap = new HashMap<>();

    public static void buildMediaFilesMap(List<MediaFileEntity> mediaFileEntities) {
        try {
            sLock.acquire();
            sMediaFilesMap.clear();
            for (MediaFileEntity mediaFileEntity : mediaFileEntities) {
                Long scheduleId = mediaFileEntity.getScheduleId();
                if (sMediaFilesMap.containsKey(scheduleId)) {
                    sMediaFilesMap.get(scheduleId).add(mediaFileEntity);
                } else {
                    ArrayList<MediaFileEntity> mediaList = new ArrayList<>();
                    mediaList.add(mediaFileEntity);
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
        Date date = new GregorianCalendar(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, hour, minute).getTime();
        mTimeStamp = DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
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

    public ArrayList<MediaFileEntity> getMediaList() {
        ArrayList<MediaFileEntity> mediaList = null;
        try {
            sLock.acquire();
            mediaList = sMediaFilesMap.get(mScheduleId);
            Collections.sort(mediaList);
            sLock.release();
        } catch (InterruptedException e) {
            // TODO
        }
        return mediaList;
    }
}
