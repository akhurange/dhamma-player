package org.dhamma.dhammaplayer.media;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.dhamma.dhammaplayer.R;
import org.dhamma.dhammaplayer.schedule.NewSchedule;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MediaSelectionAdapter extends ArrayAdapter<MediaSelection.MediaFile> {
    private Context mContext;
    private SimpleExoPlayer mSimpleExoPlayer;
    private boolean mIsPlaying;
    private ImageButton mImageButton;
    private String mMediaType;
    private Set<MediaSelection.MediaFile> mSelectedMediaSet;

    public MediaSelectionAdapter(Activity context, ArrayList<MediaSelection.MediaFile> mediaFilesList) {
        super(context, 0, mediaFilesList);
        mContext = context;
        mIsPlaying = false;
        mSelectedMediaSet = new HashSet<>();
    }

    public void setExoPlayer(SimpleExoPlayer simpleExoPlayer) {
        mSimpleExoPlayer = simpleExoPlayer;
        mIsPlaying = false;
        mSimpleExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState){
                    case ExoPlayer.STATE_ENDED:
                        mImageButton.setImageResource(android.R.drawable.ic_media_play);
                        mImageButton.setTag(mContext.getString(R.string.media_play));
                        break;
                    case ExoPlayer.STATE_READY:
                        break;
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                String TAG = "Exoplayer";

                switch (error.type) {
                    case ExoPlaybackException.TYPE_SOURCE:
                        Log.e(TAG, "TYPE_SOURCE: " + error.getSourceException().getMessage());
                        break;

                    case ExoPlaybackException.TYPE_RENDERER:
                        Log.e(TAG, "TYPE_RENDERER: " + error.getRendererException().getMessage());
                        break;

                    case ExoPlaybackException.TYPE_UNEXPECTED:
                        Log.e(TAG, "TYPE_UNEXPECTED: " + error.getUnexpectedException().getMessage());
                        break;
                }
            }
        });
    }

    public void setMediaType(String mediaType) {
        mMediaType = mediaType;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (null == listItemView) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.media_item_audio, parent, false);
        }
        if (mMediaType.equals(MediaPlayer.MEDIA_TYPE_AUDIO)) {
            return buildAudioItem(listItemView, position);
        } else {
            return buildVideoItem(listItemView, position);
        }
    }

    private View buildAudioItem(View listItemView, int position) {
        CheckBox cbMediaFile = listItemView.findViewById(R.id.cbMediaFile);
        ImageButton btPlayPause = listItemView.findViewById(R.id.btPlayPause);

        final MediaSelection.MediaFile currentMediaFile = getItem(position);
        cbMediaFile.setText(currentMediaFile.mTitle);

        btPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton ib = (ImageButton)v;
                String buttonState = (String) ib.getTag();
                if (mContext.getString(R.string.media_play).equals(buttonState)) {
                    ib.setImageResource(android.R.drawable.ic_media_pause);
                    ib.setTag(mContext.getString(R.string.media_pause));
                    DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext,
                            Util.getUserAgent(mContext, mContext.getString(R.string.app_name)));
                    MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(Uri.fromFile(new File(currentMediaFile.mFilePath)));
                    if (mIsPlaying) {
                        // If some other song was already playing, stop it.
                        mSimpleExoPlayer.setPlayWhenReady(false);
                        // Mark that particular imagebutton to play.
                        mImageButton.setImageResource(android.R.drawable.ic_media_play);
                        mImageButton.setTag(mContext.getString(R.string.media_play));
                    }
                    mSimpleExoPlayer.prepare(mediaSource);
                    mIsPlaying = true;
                    mImageButton = (ImageButton)v;
                } else {
                    mIsPlaying = false;
                    mImageButton = null;
                    ib.setImageResource(android.R.drawable.ic_media_play);
                    ib.setTag(mContext.getString(R.string.media_play));
                }
                mSimpleExoPlayer.setPlayWhenReady(mIsPlaying);
            }
        });

        cbMediaFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    // Media file selected add to the set.
                    mSelectedMediaSet.add(currentMediaFile);
                } else {
                    // Media file de selected, remove from the set.
                    mSelectedMediaSet.remove(currentMediaFile);
                }
            }
        });
        return listItemView;
    }

/*
    private View buildVideoItem(View listItemView, int position) {
        CheckBox cbMediaFile = listItemView.findViewById(R.id.cbMediaFile);
        ImageView ivVideoPreview = listItemView.findViewById(R.id.ivVideoPreview);

        final MediaSelection.MediaFile currentMediaFile = getItem(position);

        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(currentMediaFile.mFilePath,
                MediaStore.Images.Thumbnails.MINI_KIND);
        ivVideoPreview.setImageBitmap(thumbnail);

        cbMediaFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    // Media file selected add to the set.
                    mSelectedMediaSet.add(currentMediaFile);
                } else {
                    // Media file de selected, remove from the set.
                    mSelectedMediaSet.remove(currentMediaFile);
                }
            }
        });
        return listItemView;
    }
*/
    public Set<MediaSelection.MediaFile> getSelectedMediaSet() {
        return mSelectedMediaSet;
    }


    private View buildVideoItem(View listItemView, int position) {
        CheckBox cbMediaFile = listItemView.findViewById(R.id.cbMediaFile);
        ImageButton btPlayPause = listItemView.findViewById(R.id.btPlayPause);

        final MediaSelection.MediaFile currentMediaFile = getItem(position);
        cbMediaFile.setText(currentMediaFile.mTitle);

        btPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Play the video player onclick.
                Intent intent = new Intent(mContext, MediaPlayer.class);
                intent.putExtra(MediaPlayer.KEY_MEDIA_TYPE, mMediaType);
                intent.putExtra(MediaPlayer.KEY_MEDIA_PATH, currentMediaFile.mFilePath);
                intent.putExtra(MediaPlayer.KEY_MEDIA_TITLE, currentMediaFile.mTitle);
                mContext.startActivity(intent);
            }
        });

        cbMediaFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    // Media file selected add to the set.
                    mSelectedMediaSet.add(currentMediaFile);
                } else {
                    // Media file de selected, remove from the set.
                    mSelectedMediaSet.remove(currentMediaFile);
                }
            }
        });
        return listItemView;
    }
}
