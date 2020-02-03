package org.dhamma.dhammaplayer.media;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.dhamma.dhammaplayer.R;

import java.io.File;

import androidx.annotation.Nullable;

public class AudioPlayerService extends Service {
    private static int NOTIFICATION_ID  = 6422;
    private final IBinder mBinder = new LocalBinder();
    private SimpleExoPlayer mPlayer;
    private String mMediaTitle;
    private String mMediaPath;
    private PlayerNotificationManager mPlayerNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayerNotificationManager.setPlayer(null);
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mMediaPath = intent.getStringExtra(MediaPlayer.KEY_MEDIA_PATH);
        mMediaTitle = intent.getStringExtra(MediaPlayer.KEY_MEDIA_TITLE);
        if (null == mPlayer) {
            startPlayer();
        }
        return mBinder;
    }

    public SimpleExoPlayer getPlayerInstance() {
        buildPlayerInstance();
        return mPlayer;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //releasePlayer();
        mMediaPath = intent.getStringExtra(MediaPlayer.KEY_MEDIA_PATH);
        mMediaTitle = intent.getStringExtra(MediaPlayer.KEY_MEDIA_TITLE);
        if (null == mPlayer) {
            startPlayer();
        }
        return START_NOT_STICKY;
    }

    private void buildPlayerInstance() {
        if (null == mPlayer) {
            mPlayer = new SimpleExoPlayer.Builder(this).build();
        }
    }

    private void startPlayer() {
        final Context context = this;
        buildPlayerInstance();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context,getString(R.string.app_name)));
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(new File(mMediaPath)));
        mPlayer.prepare(mediaSource);
        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (ExoPlayer.STATE_ENDED == playbackState) {
                    stopSelf();
                }
            }
        });
        mPlayer.setPlayWhenReady(true);
        createNotificationChannel();
        mPlayerNotificationManager = new PlayerNotificationManager(context,
                getString(R.string.CHANNEL_ID),
                NOTIFICATION_ID,
                new DescriptionAdapter(),
                new NotificationListener());
        mPlayerNotificationManager.setPlayer(mPlayer);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.CHANNEL_ID), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public class LocalBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }

    private class DescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {

        @Override
        public String getCurrentContentTitle(Player player) {
            return mMediaTitle;
        }

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            /*
            Intent intent = new Intent(AudioPlayerService.this, MediaPlayer.class);
            intent.putExtra(MediaPlayer.KEY_MEDIA_PATH, mMediaPath);
            intent.putExtra(MediaPlayer.KEY_MEDIA_TITLE, mMediaTitle);
            return PendingIntent.getActivity(AudioPlayerService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT); */
            return null;
        }

        @Nullable
        @Override
        public String getCurrentContentText(Player player) {
            return null;
        }

        @Nullable
        @Override
        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
            return null;
        }
    }

    private class NotificationListener implements PlayerNotificationManager.NotificationListener {
        @Override
        public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
            startForeground(notificationId, notification);
        }

        @Override
        public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
            stopSelf();
        }
    }
}