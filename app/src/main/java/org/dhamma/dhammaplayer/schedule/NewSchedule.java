package org.dhamma.dhammaplayer.schedule;

import androidx.annotation.Nullable;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.dhamma.dhammaplayer.BaseActivity;
import org.dhamma.dhammaplayer.DataRepository;
import org.dhamma.dhammaplayer.R;
import org.dhamma.dhammaplayer.database.MediaFileEntity;
import org.dhamma.dhammaplayer.database.ScheduleEntity;
import org.dhamma.dhammaplayer.media.MediaPlayer;
import org.dhamma.dhammaplayer.media.MediaSelection;

import java.util.ArrayList;
import java.util.Calendar;

public class NewSchedule extends BaseActivity implements View.OnClickListener {

    class TimeDialog {
        private EditText mEtView;
        private int mHour;
        private int mMinute;

        TimeDialog(Context context, EditText et) {
            mEtView = et;
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int minute = Calendar.getInstance().get(Calendar.MINUTE);
            TimePickerDialog tpDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    mHour = selectedHour;
                    mMinute = selectedMinute;
                    String AM_PM;
                    if (selectedHour < 12) {
                        AM_PM = "AM";
                    } else {
                        AM_PM = "PM";
                        selectedHour -= 12;
                    }
                    String displayText = selectedHour + ":" + String.format("%02d", selectedMinute) + " " + AM_PM;
                    mEtView.setText(displayText);
                }
            }, hour, minute, false);
            tpDialog.show();
        }

        public int getHour() {
            return mHour;
        }

        public int getMinute() {
            return mMinute;
        }
    }

    private TimeDialog mTd;
    private DataRepository mDataRepository;
    private String mMediaType;
    private ArrayList<MediaSelection.MediaFile> mMediaFilesList;
    private static final int SELECT_MEDIA_FILES_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataRepository = new DataRepository(this);
        setContentView(R.layout.activity_new_schedule);
        findViewById(R.id.etTime).setOnClickListener(this);
        findViewById(R.id.btSaveSchedule).setOnClickListener(this);
        findViewById(R.id.rbAudio).setOnClickListener(this);
        findViewById(R.id.rbVideo).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ((RadioGroup)findViewById(R.id.rgMediaSelector)).clearCheck();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((RadioGroup)findViewById(R.id.rgMediaSelector)).clearCheck();
    }

    private boolean validateEntries() {
        boolean result = true;
        EditText etLabel = findViewById(R.id.etLabel);
        if (etLabel.getText().toString().isEmpty()) {
            etLabel.setError("Required!");
            result = false;
        } else {
            etLabel.setError(null);
        }
        EditText etTime = findViewById(R.id.etTime);
        if (etTime.getText().toString().isEmpty()) {
            etTime.setError("Required!");
            result = false;
        } else {
            etTime.setError(null);
        }
        TextView tvMediaList = (TextView)findViewById(R.id.tvSelectMediaFiles);
        if ((null == mMediaFilesList) || (0 == mMediaFilesList.size())) {
            tvMediaList.setError("Required!");
            result = false;
        } else {
            tvMediaList.setError(null);
        }
        return result;
    }

    private void saveSchedule() {
        if (!validateEntries()) {
            Toast.makeText(this, "Verify the entries.", Toast.LENGTH_SHORT).show();
            return;
        }
        EditText etLabel = findViewById(R.id.etLabel);
        boolean[] day = new boolean[7];
        day[0] = ((CheckBox)findViewById(R.id.cbSun)).isChecked();
        day[1] = ((CheckBox)findViewById(R.id.cbMon)).isChecked();
        day[2] = ((CheckBox)findViewById(R.id.cbTue)).isChecked();
        day[3] = ((CheckBox)findViewById(R.id.cbWed)).isChecked();
        day[4] = ((CheckBox)findViewById(R.id.cbThu)).isChecked();
        day[5] = ((CheckBox)findViewById(R.id.cbFri)).isChecked();
        day[6] = ((CheckBox)findViewById(R.id.cbSat)).isChecked();
        int days = 0;
        for (int i=0; i<7; i++) {
            if (day[i]) {
                days |= (0x1 << i);
            }
        }

        ScheduleEntity scheduleEntity = new ScheduleEntity(etLabel.getText().toString(),
                mTd.getHour(), mTd.getMinute(), days, mMediaType);
        mDataRepository.insertSchedule(scheduleEntity, new DataRepository.GetPrimaryKeyDatabaseWriteComplete() {
            @Override
            public void onComplete(long key) {
                MediaFileEntity[] mediaFileEntities = new MediaFileEntity[mMediaFilesList.size()];
                for (int i=0; i<mMediaFilesList.size(); i++) {
                    mediaFileEntities[i] = new MediaFileEntity(key, mMediaFilesList.get(i).mFilePath, mMediaFilesList.get(i).mTitle);
                }
                mDataRepository.insertMediaFile(mediaFileEntities, new DataRepository.OnDatabaseWriteComplete() {
                    @Override
                    public void onComplete() {
                        finish();
                    }
                });
            }
        });
    }

    private void selectAudioMediaFiles() {
        Intent intent = new Intent(this, MediaSelection.class);
        intent.putExtra(MediaPlayer.KEY_MEDIA_TYPE, MediaPlayer.MEDIA_TYPE_AUDIO);
        startActivityForResult(intent, SELECT_MEDIA_FILES_REQUEST);
    }

    private void selectVideoMediaFiles() {
        Intent intent = new Intent(this, MediaSelection.class);
        intent.putExtra(MediaPlayer.KEY_MEDIA_TYPE, MediaPlayer.MEDIA_TYPE_VIDEO);
        startActivityForResult(intent, SELECT_MEDIA_FILES_REQUEST);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.etTime:
                mTd = new TimeDialog(NewSchedule.this, (EditText)v);
                break;
            case R.id.btSaveSchedule:
                saveSchedule();
                break;
            case R.id.rbAudio:
                mMediaType = MediaPlayer.MEDIA_TYPE_AUDIO;
                selectAudioMediaFiles();
                break;
            case R.id.rbVideo:
                mMediaType = MediaPlayer.MEDIA_TYPE_VIDEO;
                selectVideoMediaFiles();
                break;
        }
    }

    private void buildMediaFilesListView() {
        ArrayList<String> mediaFiles = new ArrayList<>();
        for (MediaSelection.MediaFile mediaFile : mMediaFilesList) {
            mediaFiles.add(mediaFile.mTitle);
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, mediaFiles);
        ListView listView = findViewById(R.id.lvMediaFiles);
        listView.setAdapter(arrayAdapter);
        ListUtils.setDynamicHeight(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(NewSchedule.this, MediaPlayer.class);
                intent.putExtra(MediaPlayer.KEY_MEDIA_TYPE, mMediaType);
                intent.putExtra(MediaPlayer.KEY_MEDIA_PATH, mMediaFilesList.get(position).mFilePath);
                intent.putExtra(MediaPlayer.KEY_MEDIA_TITLE, mMediaFilesList.get(position).mTitle);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (SELECT_MEDIA_FILES_REQUEST == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                findViewById(R.id.rgMediaSelector).setVisibility(View.GONE);
                Bundle bundle = data.getExtras();
                mMediaFilesList = (ArrayList<MediaSelection.MediaFile>) bundle.getSerializable(MediaSelection.MEDIA_LIST);
                buildMediaFilesListView();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
