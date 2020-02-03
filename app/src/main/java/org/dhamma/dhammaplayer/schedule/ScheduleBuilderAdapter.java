package org.dhamma.dhammaplayer.schedule;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.dhamma.dhammaplayer.DataRepository;
import org.dhamma.dhammaplayer.R;
import org.dhamma.dhammaplayer.database.ScheduleEntity;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

public class ScheduleBuilderAdapter extends ArrayAdapter<ScheduleEntity> {
    private Activity mContext;

    public ScheduleBuilderAdapter(Activity context, ArrayList<ScheduleEntity> scheduleEntities) {
        super(context, 0, scheduleEntities);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (null == listItemView){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.schedule_group, parent, false);
        }

        TextView tvLabel = listItemView.findViewById(R.id.tvLabel);
        TextView tvTime = listItemView.findViewById(R.id.tvTime);
        SwitchCompat swActiveSchedule = listItemView.findViewById(R.id.swSchedule);
        Button btDelete = listItemView.findViewById(R.id.btDelete);

        final ScheduleEntity currentSchedule = getItem(position);
        String label = currentSchedule.getLabel();
        int hour = currentSchedule.getHour();
        int minute = currentSchedule.getMinute();
        boolean activeStatus = currentSchedule.getActiveStatus();

        tvLabel.setText(label);
        String AM_PM;
        if (hour < 12) {
            AM_PM = "AM";
        } else {
            AM_PM = "PM";
            hour -= 12;
        }
        final String timeStamp = hour+":"+String.format("%02d",minute)+" "+AM_PM;
        tvTime.setText(timeStamp);
        if (activeStatus) {
            swActiveSchedule.setChecked(true);
            tvTime.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        } else {
            swActiveSchedule.setChecked(false);
            tvTime.setTextColor(Color.GRAY);
        }

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
        return listItemView;
    }
}
