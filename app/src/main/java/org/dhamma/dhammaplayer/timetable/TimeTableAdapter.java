package org.dhamma.dhammaplayer.timetable;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.dhamma.dhammaplayer.DataRepository;
import org.dhamma.dhammaplayer.R;
import org.dhamma.dhammaplayer.database.MediaFileEntity;
import org.dhamma.dhammaplayer.database.ScheduleEntity;
import org.dhamma.dhammaplayer.media.MediaPlayer;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TimeTableAdapter extends ArrayAdapter<ScheduleEntity> {
    private Activity mContext;

    public TimeTableAdapter(Activity context, ArrayList<ScheduleEntity> scheduleEntities) {
        super(context, 0, scheduleEntities);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (null == listItemView) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.event_item, parent, false);
        }

        TextView tvScheduleLabel = listItemView.findViewById(R.id.tvScheduleLabel);
        TextView tvTimeStamp = listItemView.findViewById(R.id.tvTimestamp);
        final TextView tvMediaTitle = listItemView.findViewById(R.id.tvMediaTitle);
        final ImageButton ibPlay = listItemView.findViewById(R.id.btPlay);

        final ScheduleEntity currentSchedule = getItem(position);
        tvScheduleLabel.setText(currentSchedule.getLabel());
        Date date = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                currentSchedule.getHour(), currentSchedule.getMinute()).getTime();
        String today = DateFormat.getDateInstance(DateFormat.SHORT).format(date);
        tvTimeStamp.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(date));

        DataRepository dataRepository = new DataRepository(mContext);
        if (!today.equals(currentSchedule.getLastDate())) {
            currentSchedule.setLastPlayed((currentSchedule.getLastPlayed()+1) % currentSchedule.getMediaCount());
            currentSchedule.setLastDate(today);
            dataRepository.updateSchedule(currentSchedule, new DataRepository.OnDatabaseWriteComplete() {
                @Override
                public void onComplete() {
                    return;
                }
            });
        }
        dataRepository.getMediaFileForIndex(currentSchedule, currentSchedule.getLastPlayed(), new DataRepository.OnMediaFileReadComplete() {
            @Override
            public void onComplete(final MediaFileEntity mediaFileEntity) {
                tvMediaTitle.setText(mediaFileEntity.getMediaTitle());
                ibPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, MediaPlayer.class);
                        intent.putExtra(MediaPlayer.KEY_MEDIA_TYPE, currentSchedule.getMediaType());
                        intent.putExtra(MediaPlayer.KEY_MEDIA_PATH, mediaFileEntity.getMediaPath());
                        intent.putExtra(MediaPlayer.KEY_MEDIA_TITLE, mediaFileEntity.getMediaTitle());
                        mContext.startActivity(intent);
                    }
                });
            }
        });
        return listItemView;
    }
}
