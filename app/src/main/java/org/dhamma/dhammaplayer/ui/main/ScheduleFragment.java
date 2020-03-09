package org.dhamma.dhammaplayer.ui.main;

import android.content.Intent;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.dhamma.dhammaplayer.R;
import org.dhamma.dhammaplayer.database.MediaFileEntity;
import org.dhamma.dhammaplayer.database.ScheduleEntity;
import org.dhamma.dhammaplayer.schedule.NewSchedule;
import org.dhamma.dhammaplayer.schedule.ScheduleBuilderAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScheduleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleFragment extends Fragment {
    private ScheduleBuilderAdapter mScheduleBuilderAdapter;
    private ScheduleViewModel mScheduleViewModel;
    private ArrayList<ScheduleEntity> mScheduleList;
    List<MediaFileEntity> mMediaFileEntities;
    private LiveData<List<ScheduleEntity>> mLiveScheduleList;
    private LiveData<List<MediaFileEntity>> mLiveMediaFileList;

    public ScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);;
        mScheduleList = new ArrayList<ScheduleEntity>();
        mMediaFileEntities = new ArrayList<MediaFileEntity>();
        mScheduleBuilderAdapter = new ScheduleBuilderAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton fab = (FloatingActionButton)getView().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewSchedule.class);
                startActivity(intent);
            }
        });
        loadMediaFileList();
        buildScheduleList();
    }

    private void loadMediaFileList() {
        mLiveMediaFileList = mScheduleViewModel.liveGetMediaFiles();
        mLiveMediaFileList.observe(getViewLifecycleOwner(), new Observer<List<MediaFileEntity>>() {
            @Override
            public void onChanged(List<MediaFileEntity> mediaFileEntities) {
                mScheduleBuilderAdapter.setMediaResourceList(mediaFileEntities);
                mScheduleBuilderAdapter.notifyDataSetChanged();
            }
        });
    }

    private void buildScheduleList() {
        mLiveScheduleList = mScheduleViewModel.liveGetSchedules();
        mLiveScheduleList.observe(getViewLifecycleOwner(), new Observer<List<ScheduleEntity>>() {
            @Override
            public void onChanged(List<ScheduleEntity> scheduleEntities) {
                mScheduleList.clear();
                mScheduleList.addAll(scheduleEntities);
                mScheduleBuilderAdapter.setScheduleList(mScheduleList);
                mScheduleBuilderAdapter.notifyDataSetChanged();
                final ExpandableListView elvSchedules = getView().findViewById(R.id.elvSchedules);
                elvSchedules.setAdapter(mScheduleBuilderAdapter);
            }
        });
    }
}
