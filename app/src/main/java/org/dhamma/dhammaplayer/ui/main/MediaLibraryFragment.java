package org.dhamma.dhammaplayer.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import org.dhamma.dhammaplayer.R;
import org.dhamma.dhammaplayer.database.MediaFileEntity;
import org.dhamma.dhammaplayer.database.ScheduleEntity;
import org.dhamma.dhammaplayer.medialibrary.MediaExpandableListAdapter;
import org.dhamma.dhammaplayer.medialibrary.MediaGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MediaLibraryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MediaLibraryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MediaLibraryFragment extends Fragment {
    private ScheduleViewModel mScheduleViewModel;
    private ArrayList<MediaGroup> mMediaGroupList;
    private LiveData<List<ScheduleEntity>> mLiveScheduleList;
    private LiveData<List<MediaFileEntity>> mLiveMediaFileList;
    private MediaExpandableListAdapter mMediaExpandableListAdapter;

    public MediaLibraryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        mMediaGroupList = new ArrayList<MediaGroup>();
        mMediaExpandableListAdapter = new MediaExpandableListAdapter(mMediaGroupList, getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media_library, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      loadMediaFileList();
      buildScheduleList();
    }

    private void loadMediaFileList() {
        mLiveMediaFileList = mScheduleViewModel.liveGetMediaFiles();
        mLiveMediaFileList.observe(getViewLifecycleOwner(), new Observer<List<MediaFileEntity>>() {
            @Override
            public void onChanged(List<MediaFileEntity> mediaFileEntities) {
                MediaGroup.buildMediaFilesMap(mediaFileEntities);
            }
        });
    }

    private void buildScheduleList() {
        mLiveScheduleList = mScheduleViewModel.liveGetSchedules();
        mLiveScheduleList.observe(getViewLifecycleOwner(), new Observer<List<ScheduleEntity>>() {
            @Override
            public void onChanged(List<ScheduleEntity> scheduleEntities) {
                mMediaGroupList.clear();
                for (ScheduleEntity scheduleEntity : scheduleEntities) {
                    mMediaGroupList.add(new MediaGroup(scheduleEntity));
                }
                mMediaExpandableListAdapter.notifyDataSetChanged();
                final ExpandableListView expandableListView = getView().findViewById(R.id.elMediaList);
                expandableListView.setAdapter(mMediaExpandableListAdapter);
            }
        });
    }
}
