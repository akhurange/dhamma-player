package org.dhamma.dhammaplayer.media;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MediaSelectionAdapter extends RecyclerView.Adapter<MediaSelectionAdapter.MediaViewHolder> {
    private Context mContext;
    private SimpleExoPlayer mSimpleExoPlayer;
    private boolean mIsPlaying;
    private ImageButton mImageButton;
    private String mMediaType;
    private Set<MediaSelection.MediaFile> mSelectedMediaSet;
    private ArrayList<MediaSelection.MediaFile> mMediaFileArrayList;
    private MenuItem mMenuActionOk;


    public MediaSelectionAdapter(Context context, ArrayList<MediaSelection.MediaFile> mediaFileArrayList) {
        mContext = context;
        mMediaFileArrayList = mediaFileArrayList;
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

    public void setActionMenu(MenuItem menu) {
        mMenuActionOk = menu;
    }

    public Set<MediaSelection.MediaFile> getSelectedMediaSet() {
        return mSelectedMediaSet;
    }

    class MediaViewHolder extends RecyclerView.ViewHolder {
        CheckBox mCheckBox;
        ImageView mImageView;
        TextView mTvMediaTitle;
        TextView mTvMediaDetails;
        ImageButton mPlayPauseButton;

        MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            mCheckBox = (CheckBox)itemView.findViewById(R.id.cbMediaFile);
            mTvMediaTitle = (TextView)itemView.findViewById(R.id.tvMediaTitle);
            mTvMediaDetails = (TextView)itemView.findViewById(R.id.tvDetails);
            if (mMediaType.equals(MediaPlayer.MEDIA_TYPE_VIDEO)) {
                mImageView = (ImageView) itemView.findViewById(R.id.ivVideoPreview);
            } else {
                mPlayPauseButton = (ImageButton)itemView.findViewById(R.id.btPlayPause);
            }
        }
    }

    @NonNull
    @Override
    public MediaSelectionAdapter.MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if (mMediaType.equals(MediaPlayer.MEDIA_TYPE_AUDIO)) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.media_item_audio, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.media_item_video, parent, false);
        }
        return new MediaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaSelectionAdapter.MediaViewHolder holder, int position) {
        final MediaSelection.MediaFile mediaFile = mMediaFileArrayList.get(position);
        holder.mTvMediaTitle.setText(mediaFile.mTitle);
        String mediaDetails = "Duration: " + mediaFile.readableDuration();
        holder.mTvMediaDetails.setText(mediaDetails);
        holder.mCheckBox.setChecked(false);
        holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    // Media file selected add to the set.
                    mSelectedMediaSet.add(mediaFile);
                    if (1 == mSelectedMediaSet.size()) {
                        mMenuActionOk.setVisible(true);
                    }
                } else {
                    // Media file de selected, remove from the set.
                    mSelectedMediaSet.remove(mediaFile);
                    if (0 == mSelectedMediaSet.size()) {
                        mMenuActionOk.setVisible(false);
                    }
                }
            }
        });
        if (mMediaType.equals(MediaPlayer.MEDIA_TYPE_AUDIO)) {
            buildAudioPreview(mediaFile, holder.mPlayPauseButton);
        } else {
            Glide
                    .with(mContext)
                    .asBitmap()
                    .load(Uri.fromFile(new File(mediaFile.mFilePath)))
                    .centerCrop()
                    .placeholder(new ColorDrawable(Color.GRAY))
                    .into(holder.mImageView);

            buildVideoPreview(mediaFile, holder.mImageView);
        }
    }

    private void buildAudioPreview(final MediaSelection.MediaFile mediaFile, ImageButton btPlayPause ) {
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
                            .createMediaSource(Uri.fromFile(new File(mediaFile.mFilePath)));
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
    }

    private void buildVideoPreview(final MediaSelection.MediaFile mediaFile, ImageView playPreview ) {
        playPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Play the video player onclick.
                Intent intent = new Intent(mContext, MediaPlayer.class);
                intent.putExtra(MediaPlayer.KEY_MEDIA_TYPE, mMediaType);
                intent.putExtra(MediaPlayer.KEY_MEDIA_PATH, mediaFile.mFilePath);
                intent.putExtra(MediaPlayer.KEY_MEDIA_TITLE, mediaFile.mTitle);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMediaFileArrayList.size();
    }
}