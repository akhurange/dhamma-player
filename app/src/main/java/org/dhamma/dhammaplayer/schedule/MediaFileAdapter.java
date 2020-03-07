package org.dhamma.dhammaplayer.schedule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.dhamma.dhammaplayer.R;
import org.dhamma.dhammaplayer.media.MediaPlayer;
import org.dhamma.dhammaplayer.media.MediaSelection;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MediaFileAdapter extends ArrayAdapter<MediaSelection.MediaFile> {
    private Context mContext;
    ArrayList<MediaSelection.MediaFile> mMediaFileArrayList;
    private String mMediaType;

    public MediaFileAdapter(Activity context, ArrayList<MediaSelection.MediaFile> mediaFileArrayList, String mediaType) {
        super(context, 0, mediaFileArrayList);
        mContext = context;
        mMediaFileArrayList = mediaFileArrayList;
        mMediaType = mediaType;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (null == listItemView) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.media_file_item, parent, false);
        }
        final MediaSelection.MediaFile currentMediaFile = getItem(position);

        TextView tvMediaTitle = listItemView.findViewById(R.id.tvMediaTitle);
        tvMediaTitle.setText(currentMediaFile.mTitle);
        ImageButton ibPlay = listItemView.findViewById(R.id.btPlay);
        ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MediaPlayer.class);
                intent.putExtra(MediaPlayer.KEY_MEDIA_TYPE, mMediaType);
                intent.putExtra(MediaPlayer.KEY_MEDIA_PATH, currentMediaFile.mFilePath);
                intent.putExtra(MediaPlayer.KEY_MEDIA_TITLE, currentMediaFile.mTitle);
                mContext.startActivity(intent);
            }
        });

        ImageButton ibUpArrow = listItemView.findViewById(R.id.btUp);
        ibUpArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (0 != position) {
                    MediaSelection.MediaFile tmp = mMediaFileArrayList.get(position-1);
                    mMediaFileArrayList.set(position-1, mMediaFileArrayList.get(position));
                    mMediaFileArrayList.set(position, tmp);
                    notifyDataSetChanged();
                }
            }
        });

        ImageButton ibDownArrow = listItemView.findViewById(R.id.btDown);
        ibDownArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaFileArrayList.size()-1 != position) {
                    MediaSelection.MediaFile tmp = mMediaFileArrayList.get(position+1);
                    mMediaFileArrayList.set(position+1, mMediaFileArrayList.get(position));
                    mMediaFileArrayList.set(position, tmp);
                    notifyDataSetChanged();
                }
            }
        });

        ImageButton ibDelete = listItemView.findViewById(R.id.btDelete);
        ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaFileArrayList.remove(position);
                notifyDataSetChanged();
            }
        });

         return listItemView;
    }
}
