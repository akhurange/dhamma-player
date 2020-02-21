package org.dhamma.dhammaplayer.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.dhamma.dhammaplayer.BaseActivity;
import org.dhamma.dhammaplayer.R;

import java.io.File;

public class MediaPlayer extends BaseActivity {

    public static String MEDIA_DESCRIPTION = "media_description";
    private PlayerView mPlayerView;
    private boolean mBound = false;
    private AudioPlayerService mService;
    private String mMediaType;
    private String mMediaTitle;
    private String mMediaPath;
    private SimpleExoPlayer mSimpleExoPlayer;

    public final static String KEY_MEDIA_TYPE = "media_type";
    public final static String MEDIA_TYPE_AUDIO = "audio";
    public final static String MEDIA_TYPE_VIDEO = "video";
    public final static String KEY_MEDIA_PATH = "media_path";
    public final static String KEY_MEDIA_TITLE = "media_title";

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) iBinder;
            mService = binder.getService();
            mBound = true;
            initializeServicePlayer();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        // Read the media type, path, title.
        mMediaType = getIntent().getStringExtra(KEY_MEDIA_TYPE);
        mMediaPath = getIntent().getStringExtra(KEY_MEDIA_PATH);
        mMediaTitle = getIntent().getStringExtra(KEY_MEDIA_TITLE);
        // Initialize player view.
        mPlayerView = (PlayerView)findViewById(R.id.pvMediaPlayer);
        mPlayerView.setUseController(true);
        mPlayerView.showController();
        mPlayerView.setControllerAutoShow(true);
        mPlayerView.setControllerHideOnTouch(false);
        if (null != mMediaType) {
            if (mMediaType.equals(MEDIA_TYPE_VIDEO)) {
                // Keep the screen on.
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                // If media type video, play it locally.
                initializeLocalPlayer();
            } else {
                // If media type audio, play it as service.
                initializeAudioService();
            }
        }
    }

    private void initializeServicePlayer() {
        if (mBound) {
            SimpleExoPlayer player = mService.getPlayerInstance();
            mPlayerView.setPlayer(player);
        }
    }

    private void initializeAudioService() {
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.putExtra(MediaPlayer.KEY_MEDIA_PATH, mMediaPath);
        intent.putExtra(MediaPlayer.KEY_MEDIA_TITLE, mMediaTitle);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStart() {
        if (null != mMediaType) {
            if (mMediaType.equals(MEDIA_TYPE_AUDIO)) {
                setUI();
            } else {
                mSimpleExoPlayer.setPlayWhenReady(true);
            }
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (mMediaType.equals(MEDIA_TYPE_VIDEO)) {
            mSimpleExoPlayer.setPlayWhenReady(true);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mMediaType.equals(MEDIA_TYPE_VIDEO)) {
            mSimpleExoPlayer.setPlayWhenReady(false);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mMediaType.equals(MEDIA_TYPE_AUDIO)) {
            unbindService(mConnection);
            mBound = false;
        } else {
            mPlayerView.setPlayer(null);
            mSimpleExoPlayer.release();
        }
        super.onDestroy();
    }

    private void setUI() {
        TextView tv = findViewById(R.id.tvMediaTitle);
        tv.setText(mMediaTitle);
    }

    private void initializeLocalPlayer() {
        mSimpleExoPlayer = new SimpleExoPlayer.Builder(this).build();
        mPlayerView.setPlayer(mSimpleExoPlayer);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getString(R.string.app_name)));
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(new File(mMediaPath)));
        mSimpleExoPlayer.prepare(mediaSource);
    }
}
