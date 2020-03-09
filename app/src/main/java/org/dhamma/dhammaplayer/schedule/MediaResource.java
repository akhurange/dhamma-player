package org.dhamma.dhammaplayer.schedule;

import android.content.Context;

import org.dhamma.dhammaplayer.DataRepository;
import org.dhamma.dhammaplayer.database.MediaFileEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaResource {
    private DataRepository mDataRepository;
    private Map<Long, ArrayList<MediaFileEntity>> mMediaFilesMap;

    public MediaResource(Context context, List<MediaFileEntity> mediaFileEntities) {
        mDataRepository = new DataRepository(context);
        mMediaFilesMap = new HashMap<Long, ArrayList<MediaFileEntity>>();
        for (MediaFileEntity mediaFileEntity : mediaFileEntities) {
            if (mMediaFilesMap.containsKey(mediaFileEntity.getScheduleId())) {
                mMediaFilesMap.get(mediaFileEntity.getScheduleId()).add(mediaFileEntity);
            } else {
                ArrayList<MediaFileEntity> mediaList = new ArrayList<MediaFileEntity>();
                mediaList.add(mediaFileEntity);
                mMediaFilesMap.put(mediaFileEntity.getScheduleId(), mediaList);
            }
        }

        for(Map.Entry<Long, ArrayList<MediaFileEntity>> entry : mMediaFilesMap.entrySet()) {
            Collections.sort(entry.getValue());
        }
    }

    public ArrayList<MediaFileEntity> getMediaResourceForSchedule(Long scheduleId) {
        return mMediaFilesMap.get(scheduleId);
    }

    public void updateMediaResourceForSchedule(Long scheduleId, ArrayList<MediaFileEntity> mediaFileEntityArrayList) {
        Collections.sort(mediaFileEntityArrayList);
        mMediaFilesMap.put(scheduleId, mediaFileEntityArrayList);
        MediaFileEntity[] mediaFileEntities = new MediaFileEntity[mediaFileEntityArrayList.size()];
        mDataRepository.insertMediaFile(mediaFileEntityArrayList.toArray(mediaFileEntities), new DataRepository.OnDatabaseWriteComplete() {
            @Override
            public void onComplete() {
                return;
            }
        });
    }
}
