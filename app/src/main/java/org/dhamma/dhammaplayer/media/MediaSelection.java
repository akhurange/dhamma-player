package org.dhamma.dhammaplayer.media;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.exoplayer2.SimpleExoPlayer;

import org.dhamma.dhammaplayer.BaseActivity;
import org.dhamma.dhammaplayer.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MediaSelection extends BaseActivity {
    static public class MediaFile implements Serializable {
        public String mTitle;
        public String mFilePath;
        int mDuration;
        Long mSize;
        public String mReadableDuration;

        private final static long ONE_SECOND = 1000;
        private final static long SECONDS = 60;
        private final static long ONE_MINUTE = ONE_SECOND * 60;
        private final static long MINUTES = 60;
        private final static long ONE_HOUR = ONE_MINUTE * 60;
        private final static long HOURS = 24;
        private final static long ONE_DAY = ONE_HOUR * 24;

        @Override
        public int hashCode() {
            return mDuration;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            return mFilePath.equals(((MediaFile)obj).mFilePath);
        }

        public void setReadableDuration() {
            mDuration /= ONE_SECOND;
            int seconds = (int) (mDuration % SECONDS);
            mDuration /= SECONDS;
            int minutes = (int) (mDuration % MINUTES);
            mDuration /= MINUTES;
            int hours = (int) (mDuration % HOURS);
            int days = (int) (mDuration / HOURS);
            if (days == 0) {
                mReadableDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            } else {
                mReadableDuration = String.format("%day(s) - %02d:%02d:%02d", days, hours, minutes, seconds);
            }
        }
    }

    private SimpleExoPlayer mSimpleExoPlayer;
    private ArrayList<MediaFile> mListElementsArrayList;
    private MediaSelectionAdapter mMediaAdapter;
    private ArrayList<String> mUsedMediaList;
    public static String MEDIA_LIST = "media_list";
    public static String USED_MEDIA_LIST = "used_media_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String mediaType = getIntent().getStringExtra(MediaPlayer.KEY_MEDIA_TYPE);
        mUsedMediaList = (ArrayList<String>) getIntent().getSerializableExtra(USED_MEDIA_LIST);
        setContentView(R.layout.activity_media_selection);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mListElementsArrayList = new ArrayList<>();
        mMediaAdapter = new MediaSelectionAdapter(MediaSelection.this, mListElementsArrayList);
        RecyclerView rvMediaFiles = (RecyclerView)findViewById(R.id.rvMediaFiles);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rvMediaFiles.setLayoutManager(layoutManager);
        rvMediaFiles.setHasFixedSize(true);
        rvMediaFiles.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvMediaFiles.getContext(),
                DividerItemDecoration.VERTICAL);
        rvMediaFiles.addItemDecoration(dividerItemDecoration);
        rvMediaFiles.setAdapter(mMediaAdapter);
        buildMediaList(mediaType);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeExoPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeExoPlayer();
    }

    @Override
    protected void onPause() {
        releaseExoPlayer();
        super.onPause();
    }

    @Override
    protected void onStop() {
        releaseExoPlayer();
        super.onStop();
    }

    private void initializeExoPlayer() {
        mSimpleExoPlayer = new SimpleExoPlayer.Builder(this).build();
        mMediaAdapter.setExoPlayer(mSimpleExoPlayer);
    }

    private void releaseExoPlayer() {
        mSimpleExoPlayer.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_media_selection, menu);
        menu.findItem(R.id.action_ok).setVisible(false);
        mMediaAdapter.setActionMenu(menu.findItem(R.id.action_ok));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_ok:
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... voids) {
                        returnSelectedMediaFileList();
                        return null;
                    }
                }.execute();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void returnSelectedMediaFileList() {
        // Get the selected media set.
        Set<MediaFile> selectedMediaSet = mMediaAdapter.getSelectedMediaSet();
        // Set to list for serialization.
        ArrayList<MediaFile> mediaList = new ArrayList<MediaFile>(selectedMediaSet);

        // Create the return intent.
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(MEDIA_LIST, mediaList);
        resultIntent.putExtras(bundle);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void buildMediaList(final String mediaType) {
        requestPermission(BaseActivity.PERMISSION_EXT_STORAGE,
                "Permission to use external storage is needed to select the media files.",
                new OnPermissionResult() {
                    @Override
                    public void takeAction(boolean result) {
                        if (!result) {
                            Toast.makeText(MediaSelection.this,
                                    "Without permission to use external storage you can not browse media files.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            mMediaAdapter.setMediaType(mediaType);
                            if (mediaType.equals(MediaPlayer.MEDIA_TYPE_AUDIO)) {
                                browseAudioMediaFiles();
                            } else {
                                browseVideoMediaFiles();
                            }
                        }
                    }
                });
    }

    private void browseAudioMediaFiles() {
        Set<String> usedMediaSet = null;
        if (null != mUsedMediaList) {
            usedMediaSet = new HashSet<>(mUsedMediaList);
        }
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        String[] projection = new String[] {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE
        };

        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null);
        if (cursor == null) {
            Toast.makeText(MediaSelection.this,"Something Went Wrong.", Toast.LENGTH_LONG).show();
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(MediaSelection.this,"No Music Found on SD Card.", Toast.LENGTH_LONG).show();
        }
        else {
            // Cache column indices.
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            do {
                if (null != usedMediaSet) {
                    // If this file is already used in the schedule do not list it again.
                    if (usedMediaSet.contains(cursor.getString(dataColumn))) {
                        continue;
                    }
                }
                MediaFile mediaFile = new MediaFile();
                mediaFile.mTitle = cursor.getString(nameColumn);
                mediaFile.mDuration = cursor.getInt(durationColumn);
                mediaFile.mSize = cursor.getLong(sizeColumn);
                mediaFile.mFilePath = cursor.getString(dataColumn);
                mediaFile.setReadableDuration();
                mListElementsArrayList.add(mediaFile);
            } while (cursor.moveToNext());
            cursor.close();
        }
        mMediaAdapter.notifyDataSetChanged();
    }

    private void browseVideoMediaFiles() {
        Set<String> usedMediaSet = null;
        if (null != mUsedMediaList) {
            usedMediaSet = new HashSet<>(mUsedMediaList);
        }
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        String[] projection = new String[] {
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE
        };

        Cursor cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null);
        if (cursor == null) {
            Toast.makeText(MediaSelection.this,"Something Went Wrong.", Toast.LENGTH_LONG).show();
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(MediaSelection.this,"No Video Found on SD Card.", Toast.LENGTH_LONG).show();
        }
        else {
            // Cache column indices.
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            do {
                if (null != usedMediaSet) {
                    // If this file is already used in the schedule do not list it again.
                    if (usedMediaSet.contains(cursor.getString(dataColumn))) {
                        continue;
                    }
                }
                MediaFile mediaFile = new MediaFile();
                mediaFile.mTitle = cursor.getString(nameColumn);
                mediaFile.mDuration = cursor.getInt(durationColumn);
                mediaFile.mSize = cursor.getLong(sizeColumn);
                mediaFile.mFilePath = cursor.getString(dataColumn);
                mediaFile.setReadableDuration();
                mListElementsArrayList.add(mediaFile);
            } while (cursor.moveToNext());
            cursor.close();
        }
        mMediaAdapter.notifyDataSetChanged();
    }
}
