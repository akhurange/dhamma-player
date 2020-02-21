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
import android.widget.ListView;
import android.widget.TextView;

import org.dhamma.dhammaplayer.BaseActivity;
import org.dhamma.dhammaplayer.R;
import org.dhamma.dhammaplayer.database.ScheduleEntity;
import org.dhamma.dhammaplayer.timetable.TimeTableAdapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TimeTableFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TimeTableFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimeTableFragment extends Fragment {
    private ScheduleViewModel mScheduleViewModel;
    private ArrayList<ScheduleEntity> mScheduleList;
    private TimeTableAdapter mTimeTableAdapter;
    private LiveData<List<ScheduleEntity>> mLiveScheduleList;

    public TimeTableFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        mScheduleList = new ArrayList<ScheduleEntity>();
        mTimeTableAdapter = new TimeTableAdapter(getActivity(), mScheduleList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvToday = getView().findViewById(R.id.tvDate);
        Date date = Calendar.getInstance().getTime();
        tvToday.setText(DateFormat.getDateInstance(DateFormat.LONG).format(date));

        mLiveScheduleList = mScheduleViewModel.liveGetSchedules();
        mLiveScheduleList.observe(getViewLifecycleOwner(), new Observer<List<ScheduleEntity>>() {
            @Override
            public void onChanged(List<ScheduleEntity> scheduleEntities) {
                mScheduleList.clear();
                mScheduleList.addAll(scheduleEntities);
                mTimeTableAdapter.notifyDataSetChanged();
                final ListView listView = getView().findViewById(R.id.lvEvents);
                listView.setAdapter(mTimeTableAdapter);
                BaseActivity.ListUtils.setDynamicHeight(listView);
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
