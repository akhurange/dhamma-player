package org.dhamma.dhammaplayer.schedule;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.material.snackbar.Snackbar;

import org.dhamma.dhammaplayer.BaseActivity;
import org.dhamma.dhammaplayer.DataRepository;
import org.dhamma.dhammaplayer.R;
import org.dhamma.dhammaplayer.database.MediaFileEntity;
import org.dhamma.dhammaplayer.database.ScheduleEntity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

public class ScheduleBuilderAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private ArrayList<ScheduleEntity> mScheduleEntityArrayList;
    private MediaResource mMediaResource;

    public ScheduleBuilderAdapter( Context context) {
        mContext = context;
    }

    public void setScheduleList(ArrayList<ScheduleEntity> scheduleEntityArrayList) {
        mScheduleEntityArrayList = scheduleEntityArrayList;
    }

    public void setMediaResourceList(List<MediaFileEntity> mediaFileEntities) {
        mMediaResource = new MediaResource(mContext, mediaFileEntities);
    }

    @Override
    public int getGroupCount() {
        return mScheduleEntityArrayList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mScheduleEntityArrayList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mScheduleEntityArrayList.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition*mScheduleEntityArrayList.size()+childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final ScheduleEntity currentSchedule = mScheduleEntityArrayList.get(groupPosition);
        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.schedule_group, parent, false);
        }

        final TextView tvTime = convertView.findViewById(R.id.tvTime);
        TextView tvLabel = convertView.findViewById(R.id.tvLabel);
        SwitchCompat swActiveSchedule = convertView.findViewById(R.id.swSchedule);

        String label = currentSchedule.getLabel();
        final int hour = currentSchedule.getHour();
        final int minute = currentSchedule.getMinute();
        boolean activeStatus = currentSchedule.getActiveStatus();

        tvLabel.setText(label);
        Date date = new GregorianCalendar(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, hour, minute).getTime();
        final String timeStamp = DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
        tvTime.setText(timeStamp);
        if (activeStatus) {
            swActiveSchedule.setChecked(true);
            tvTime.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        } else {
            swActiveSchedule.setChecked(false);
            tvTime.setTextColor(Color.GRAY);
        }

        // Allow change of time for schedule.
        tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog tpDialog = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        Date date = new GregorianCalendar(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH,
                                selectedHour, selectedMinute).getTime();
                        tvTime.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(date));
                        // Save new changes to local db.
                        DataRepository dataRepository = new DataRepository(mContext);
                        ScheduleEntity scheduleEntity = new ScheduleEntity(currentSchedule.getLabel(), selectedHour, selectedMinute,
                                currentSchedule.getDays(), currentSchedule.getMediaType(),
                                currentSchedule.getMediaCount(), currentSchedule.getLastDate());
                        scheduleEntity.setKey(currentSchedule.getKey());
                        dataRepository.updateSchedule(scheduleEntity, new DataRepository.OnDatabaseWriteComplete() {
                            @Override
                            public void onComplete() {
                                return;
                            }
                        });
                    }
                }, hour, minute, false);
                tpDialog.show();
            }
        });

        swActiveSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String warning = "Alarm for " + currentSchedule.getLabel() + " at " + timeStamp + " turned ";
                warning += (((SwitchCompat)v).isChecked() ? "on" : "off");
                Snackbar snackbar = Snackbar.make(((Activity)mContext).findViewById(R.id.coordinatorLayout), warning, Snackbar.LENGTH_SHORT);
                snackbar.show();
                currentSchedule.setActiveStatus(((SwitchCompat)v).isChecked());
                DataRepository dataRepository = new DataRepository(mContext);
                dataRepository.updateSchedule(currentSchedule, new DataRepository.OnDatabaseWriteComplete() {
                    @Override
                    public void onComplete() {
                        return;
                    }
                });
            }
        });
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ScheduleEntity currentSchedule = mScheduleEntityArrayList.get(groupPosition);
        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.schedule_child, parent, false);
        }

        final ScheduleEntity scheduleEntity = mScheduleEntityArrayList.get(groupPosition);
        MediaResourceAdapter mediaResourceAdapter = new MediaResourceAdapter(mContext,
                mMediaResource.getMediaResourceForSchedule(scheduleEntity.getKey()),
                scheduleEntity.getMediaType());
        final ListView listView = convertView.findViewById(R.id.lvMediaFiles);
        listView.setAdapter(mediaResourceAdapter);
        BaseActivity.ListUtils.setDynamicHeight(listView);

        Button btDelete = convertView.findViewById(R.id.btDelete);
        btDelete.setFocusable(false);

        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataRepository dataRepository = new DataRepository(mContext);
                dataRepository.deleteSchedule(currentSchedule, new DataRepository.OnDatabaseWriteComplete() {
                    @Override
                    public void onComplete() {
                        return;
                    }
                });
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}