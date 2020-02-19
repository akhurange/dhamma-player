package org.dhamma.dhammaplayer.schedule;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.dhamma.dhammaplayer.DataRepository;
import org.dhamma.dhammaplayer.R;
import org.dhamma.dhammaplayer.database.ScheduleEntity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

public class ScheduleBuilderAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private ArrayList<ScheduleEntity> mScheduleEntityArrayList;

    public ScheduleBuilderAdapter(ArrayList<ScheduleEntity> scheduleEntityArrayList, Context context) {
        mScheduleEntityArrayList = scheduleEntityArrayList;
        mContext = context;
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

        TextView tvTime = convertView.findViewById(R.id.tvTime);
        TextView tvLabel = convertView.findViewById(R.id.tvLabel);
        SwitchCompat swActiveSchedule = convertView.findViewById(R.id.swSchedule);

        String label = currentSchedule.getLabel();
        int hour = currentSchedule.getHour();
        int minute = currentSchedule.getMinute();
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

        swActiveSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String warning = "Alarm for " + currentSchedule.getLabel() + " at " + timeStamp + " turned ";
                warning += (((SwitchCompat)v).isChecked() ? "on" : "off");
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