package org.dhamma.dhammaplayer;

import android.content.Context;
import android.os.AsyncTask;

import org.dhamma.dhammaplayer.database.AppDatabase;
import org.dhamma.dhammaplayer.database.MediaFileDao;
import org.dhamma.dhammaplayer.database.MediaFileEntity;
import org.dhamma.dhammaplayer.database.ScheduleDao;
import org.dhamma.dhammaplayer.database.ScheduleEntity;

import java.util.List;

import androidx.lifecycle.LiveData;

public class DataRepository {
    public interface OnDatabaseWriteComplete {
        void onComplete();
    }

    public interface OnMediaFileReadComplete {
        void onComplete(MediaFileEntity mediaFileEntity);
    }

    public interface GetPrimaryKeyDatabaseWriteComplete {
        void onComplete(long key);
    }

    private AppDatabase mAppDatabase;

    public DataRepository(Context context) {
        mAppDatabase = AppDatabase.getInstance(context);
    }

    //***** Async operation for inserting schedule. *****//
    public void insertSchedule(ScheduleEntity scheduleEntity, final GetPrimaryKeyDatabaseWriteComplete callback) {
        ScheduleDao dao = mAppDatabase.scheduleDao();
        new addScheduleLocalDbAsync(dao, scheduleEntity, new addScheduleLocalDbAsync.AsyncResponse() {
            @Override
            public void processFinish(long key) {
                callback.onComplete(key);
            }
        }).execute();
    }

    private static class addScheduleLocalDbAsync extends AsyncTask<Void, Void, Long> {
        public interface AsyncResponse {
            void processFinish(long key);
        }

        private ScheduleDao mDao;
        private ScheduleEntity mScheduleEntity;
        private AsyncResponse mCallback;

        addScheduleLocalDbAsync(ScheduleDao dao, ScheduleEntity scheduleEntity, AsyncResponse callback) {
            mDao = dao;
            mScheduleEntity = scheduleEntity;
            mCallback = callback;
        }

        @Override
        protected Long doInBackground(Void... voids) {
            return mDao.insertSchedule(mScheduleEntity);
        }

        @Override
        protected void onPostExecute(Long key) {
            mCallback.processFinish(key);
        }
    }

    //***** Async operation for updating schedule. *****//
    public void updateSchedule(ScheduleEntity scheduleEntity, final OnDatabaseWriteComplete callback) {
        ScheduleDao dao = mAppDatabase.scheduleDao();
        new updateScheduleLocalDbAsync(dao, scheduleEntity, new updateScheduleLocalDbAsync.AsyncResponse() {
            @Override
            public void processFinish() {
                callback.onComplete();
            }
        }).execute();
    }

    private static class updateScheduleLocalDbAsync extends AsyncTask<Void, Void, Void> {
        public interface AsyncResponse {
            void processFinish();
        }

        private ScheduleDao mDao;
        private ScheduleEntity mScheduleEntity;
        private AsyncResponse mCallback;

        updateScheduleLocalDbAsync(ScheduleDao dao, ScheduleEntity scheduleEntity, AsyncResponse callback) {
            mDao = dao;
            mScheduleEntity = scheduleEntity;
            mCallback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mDao.updateSchedule(mScheduleEntity);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mCallback.processFinish();
        }
    }

    //***** Async operation for deleting schedule. *****//
    public void deleteSchedule(final ScheduleEntity scheduleEntity, final OnDatabaseWriteComplete callback) {
        ScheduleDao dao = mAppDatabase.scheduleDao();
        new deleteScheduleLocalDbAsync(dao, scheduleEntity, new deleteScheduleLocalDbAsync.AsyncResponse() {
            @Override
            public void processFinish() {
                deleteMediaFiles(scheduleEntity, new OnDatabaseWriteComplete() {
                    @Override
                    public void onComplete() {
                        callback.onComplete();
                    }
                });
            }
        }).execute();
    }

    private static class deleteScheduleLocalDbAsync extends AsyncTask<Void, Void, Void> {
        public interface AsyncResponse {
            void processFinish();
        }

        private ScheduleDao mDao;
        private ScheduleEntity mScheduleEntity;
        private AsyncResponse mCallback;

        deleteScheduleLocalDbAsync(ScheduleDao dao, ScheduleEntity scheduleEntity, AsyncResponse callback) {
            mDao = dao;
            mScheduleEntity = scheduleEntity;
            mCallback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mDao.deleteSchedule(mScheduleEntity);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mCallback.processFinish();
        }
    }

    //***** Get live schedule summary. *****//
    public LiveData<List<ScheduleEntity>> getLiveSchedules() {
        return mAppDatabase.scheduleDao().liveLoadSchedules();
    }

    //***** Async operation for inserting media files. *****//
    public void insertMediaFile(MediaFileEntity[] mediaFileEntities, final OnDatabaseWriteComplete callback) {
        MediaFileDao dao = mAppDatabase.mediaFileDao();
        new addMediaFileLocalDbAsync(dao, mediaFileEntities, new addMediaFileLocalDbAsync.AsyncResponse() {
            @Override
            public void processFinish() {
                callback.onComplete();
            }
        }).execute();
    }

    private static class addMediaFileLocalDbAsync extends AsyncTask<Void, Void, Void> {
        public interface AsyncResponse {
            void processFinish();
        }

        private MediaFileDao mDao;
        private MediaFileEntity[] mMediaFileEntities;
        private AsyncResponse mCallback;

        addMediaFileLocalDbAsync(MediaFileDao dao, MediaFileEntity[] mediaFileEntities, AsyncResponse callback) {
            mDao = dao;
            mMediaFileEntities = mediaFileEntities;
            mCallback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mDao.insertMediaFiles(mMediaFileEntities);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mCallback.processFinish();
        }
    }

    //***** Async operation for deleting media files of a schedule. *****//
    public void deleteMediaFiles(ScheduleEntity scheduleEntity, final OnDatabaseWriteComplete callback) {
        MediaFileDao dao = mAppDatabase.mediaFileDao();
        new deleteMediaFilesLocalDbAsync(dao, scheduleEntity, new deleteMediaFilesLocalDbAsync.AsyncResponse() {
            @Override
            public void processFinish() {
                callback.onComplete();
            }
        }).execute();
    }

    private static class deleteMediaFilesLocalDbAsync extends AsyncTask<Void, Void, Void> {
        public interface AsyncResponse {
            void processFinish();
        }

        private MediaFileDao mDao;
        private ScheduleEntity mScheduleEntity;
        private AsyncResponse mCallback;

        deleteMediaFilesLocalDbAsync(MediaFileDao dao, ScheduleEntity scheduleEntity, AsyncResponse callback) {
            mDao = dao;
            mScheduleEntity = scheduleEntity;
            mCallback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mDao.deleteMediaFilesForSchedule(mScheduleEntity.getKey());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mCallback.processFinish();
        }
    }

    //***** Async operation for reading i'th media file of a schedule. *****//
    public void getMediaFileForIndex(ScheduleEntity scheduleEntity, int index, final OnMediaFileReadComplete callback) {
        MediaFileDao dao = mAppDatabase.mediaFileDao();
        new readMediaFileForIndexLocalDbAsync(dao, scheduleEntity, index, new readMediaFileForIndexLocalDbAsync.AsyncResponse() {
            @Override
            public void processFinish(MediaFileEntity mediaFileEntity) {
                callback.onComplete(mediaFileEntity);
            }
        }).execute();
    }

    private static class readMediaFileForIndexLocalDbAsync extends AsyncTask<Void, Void, MediaFileEntity> {
        public interface AsyncResponse {
            void processFinish(MediaFileEntity mediaFileEntity);
        }

        private MediaFileDao mDao;
        private ScheduleEntity mScheduleEntity;
        private int mIndex;
        private AsyncResponse mCallback;

        readMediaFileForIndexLocalDbAsync(MediaFileDao dao, ScheduleEntity scheduleEntity, int index, AsyncResponse callback) {
            mDao = dao;
            mScheduleEntity = scheduleEntity;
            mIndex = index;
            mCallback = callback;
        }

        @Override
        protected MediaFileEntity doInBackground(Void... voids) {
            return mDao.loadMediaFileForScheduleForIndex(mScheduleEntity.getKey(), mIndex);
        }

        @Override
        protected void onPostExecute(MediaFileEntity mediaFileEntity) {
            mCallback.processFinish(mediaFileEntity);
        }
    }

    //***** Get live form summary. *****//
    public LiveData<List<MediaFileEntity>> getLiveMediaFiles() {
        return mAppDatabase.mediaFileDao().liveLoadMediaFiles();
    }
}
