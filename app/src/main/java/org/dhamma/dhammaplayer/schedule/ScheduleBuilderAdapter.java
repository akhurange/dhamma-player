package org.dhamma.dhammaplayer.schedule;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
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
import org.dhamma.dhammaplayer.media.MediaPlayer;
import org.dhamma.dhammaplayer.media.MediaSelection;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class ScheduleBuilderAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private Fragment mParentFragment;
    private ArrayList<ScheduleEntity> mScheduleEntityArrayList;
    private MediaResource mMediaResource;
    private ScheduleEntity mCurrentSchedule;
    private int mPreviousExpandedGroup;
    private static final int SELECT_MEDIA_FILES_REQUEST = 1;

    public ScheduleBuilderAdapter(Context context, Fragment fragment) {
        mContext = context;
        mParentFragment = fragment;
        mPreviousExpandedGroup = -1;
    }

    public void setScheduleList(ArrayList<ScheduleEntity> scheduleEntityArrayList) {
        mScheduleEntityArrayList = scheduleEntityArrayList;
    }

    public void setMediaResourceList(List<MediaFileEntity> mediaFileEntities) {
        mMediaResource = new MediaResource(mContext, mediaFileEntities);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
        mPreviousExpandedGroup = groupPosition;
    }

    public int getPreviousExpandedGroup() {
        return mPreviousExpandedGroup;
    }

    @Override
    public int getGroupCount() {
        if (null == mScheduleEntityArrayList) {
            return 0;
        }
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
        MediaResourceAdapter mediaResourceAdapter = new MediaResourceAdapter(mContext,
                mMediaResource.getMediaResourceForSchedule(currentSchedule.getKey()),
                currentSchedule);
        final ListView listView = convertView.findViewById(R.id.lvMediaFiles);
        listView.setAdapter(mediaResourceAdapter);
        BaseActivity.ListUtils.setDynamicHeight(listView);

        Button btAddMedia = convertView.findViewById(R.id.btAddMedia);
        btAddMedia.setFocusable(false);
        btAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                browseMediaFiles(currentSchedule);
            }
        });

        Button btDelete = convertView.findViewById(R.id.btDelete);
        btDelete.setFocusable(false);
        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSchedule(currentSchedule);
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private void deleteSchedule(ScheduleEntity currentSchedule) {
        DataRepository dataRepository = new DataRepository(mContext);
        dataRepository.deleteSchedule(currentSchedule, new DataRepository.OnDatabaseWriteComplete() {
            @Override
            public void onComplete() {
                return;
            }
        });
    }

    private void browseMediaFiles(ScheduleEntity currentSchedule) {
        // Save the current schedule for future reference o work onActivityResult.
        mCurrentSchedule = currentSchedule;
        ArrayList<MediaFileEntity> mediaFileEntityArrayList = mMediaResource.getMediaResourceForSchedule(currentSchedule.getKey());
        Intent intent = new Intent(mContext, MediaSelection.class);
        intent.putExtra(MediaPlayer.KEY_MEDIA_TYPE, currentSchedule.getMediaType());
        if (null != mediaFileEntityArrayList) {
            ArrayList<String> usedMediaList = new ArrayList<String>();
            for (MediaFileEntity mediaFileEntity : mediaFileEntityArrayList) {
                usedMediaList.add(mediaFileEntity.getMediaPath());
            }
            intent.putExtra(MediaSelection.USED_MEDIA_LIST, usedMediaList);
        }
        mParentFragment.startActivityForResult(intent, SELECT_MEDIA_FILES_REQUEST);
    }

    public void addMediaResource(ArrayList<MediaSelection.MediaFile> mediaFilesList) {
        final ArrayList<MediaFileEntity> mediaFileEntityArrayList = new ArrayList<>();
        int nextMediaIndex = mCurrentSchedule.getMediaCount();
        for (MediaSelection.MediaFile mediaFile : mediaFilesList) {
            mediaFileEntityArrayList.add(new MediaFileEntity(mCurrentSchedule.getKey(), mediaFile.mFilePath, mediaFile.mTitle, nextMediaIndex));
            nextMediaIndex++;
        }

        MediaFileEntity[] mediaFileEntities = new MediaFileEntity[mediaFileEntityArrayList.size()];
        final DataRepository dataRepository = new DataRepository(mContext);
        dataRepository.insertMediaFile(mediaFileEntityArrayList.toArray(mediaFileEntities), new DataRepository.OnDatabaseWriteComplete() {
            @Override
            public void onComplete() {
                mCurrentSchedule.setMediaCount(mediaFileEntityArrayList.size());
                dataRepository.updateSchedule(mCurrentSchedule, new DataRepository.OnDatabaseWriteComplete() {
                    @Override
                    public void onComplete() {
                        return;
                    }
                });
            }
        });
    }
}