package org.dhamma.dhammaplayer.ui.main;

import android.app.Application;

import org.dhamma.dhammaplayer.DataRepository;
import org.dhamma.dhammaplayer.database.MediaFileEntity;
import org.dhamma.dhammaplayer.database.ScheduleEntity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class ScheduleViewModel extends AndroidViewModel {
    private DataRepository mDataRepository;

    public ScheduleViewModel(@NonNull Application application) {
        super(application);
        mDataRepository = new DataRepository(application.getApplicationContext());
    }

    public LiveData<List<ScheduleEntity>> liveGetSchedules() {
        return mDataRepository.getLiveSchedules();
    }

    public LiveData<List<MediaFileEntity>> liveGetMediaFiles() {
        return mDataRepository.getLiveMediaFiles();
    }
}
