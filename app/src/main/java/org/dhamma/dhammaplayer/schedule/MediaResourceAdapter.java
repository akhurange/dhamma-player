package org.dhamma.dhammaplayer.schedule;

import android.content.Context;
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
import org.dhamma.dhammaplayer.media.MediaPlayer;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MediaResourceAdapter extends ArrayAdapter<MediaFileEntity> {
    private Context mContext;
    private ArrayList<MediaFileEntity> mMediaFileEntityArrayList;
    private String mMediaType;
    private DataRepository mDataRepository;

    public MediaResourceAdapter(Context context, ArrayList<MediaFileEntity> mediaFileArrayList, String mediaType) {
        super(context, 0, mediaFileArrayList);
        mContext = context;
        mMediaFileEntityArrayList = mediaFileArrayList;
        mMediaType = mediaType;
        mDataRepository = new DataRepository(mContext);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (null == listItemView) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.media_file_item, parent, false);
        }
        final MediaFileEntity currentMediaFile = getItem(position);

        TextView tvMediaTitle = listItemView.findViewById(R.id.tvMediaTitle);
        tvMediaTitle.setText(currentMediaFile.getMediaTitle());
        ImageButton ibPlay = listItemView.findViewById(R.id.btPlay);
        ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MediaPlayer.class);
                intent.putExtra(MediaPlayer.KEY_MEDIA_TYPE, mMediaType);
                intent.putExtra(MediaPlayer.KEY_MEDIA_PATH, currentMediaFile.getMediaPath());
                intent.putExtra(MediaPlayer.KEY_MEDIA_TITLE, currentMediaFile.getMediaTitle());
                mContext.startActivity(intent);
            }
        });

        ImageButton ibUpArrow = listItemView.findViewById(R.id.btUp);
        ibUpArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (0 != position) {
                    MediaFileEntity tmp = mMediaFileEntityArrayList.get(position-1);
                    mMediaFileEntityArrayList.set(position-1, mMediaFileEntityArrayList.get(position));
                    mMediaFileEntityArrayList.set(position, tmp);
                    mMediaFileEntityArrayList.get(position-1).setScheduleIndex(position-1);
                    mMediaFileEntityArrayList.get(position).setScheduleIndex(position);
                    updateMediaFilesList();
                }
            }
        });

        ImageButton ibDownArrow = listItemView.findViewById(R.id.btDown);
        ibDownArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaFileEntityArrayList.size()-1 != position) {
                    MediaFileEntity tmp = mMediaFileEntityArrayList.get(position+1);
                    mMediaFileEntityArrayList.set(position+1, mMediaFileEntityArrayList.get(position));
                    mMediaFileEntityArrayList.set(position, tmp);
                    mMediaFileEntityArrayList.get(position+1).setScheduleIndex(position+1);
                    mMediaFileEntityArrayList.get(position).setScheduleIndex(position);
                    updateMediaFilesList();
                }
            }
        });

        ImageButton ibDelete = listItemView.findViewById(R.id.btDelete);
        ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataRepository.deleteMediaFile(mMediaFileEntityArrayList.get(position), new DataRepository.OnDatabaseWriteComplete() {
                    @Override
                    public void onComplete() {
                        mMediaFileEntityArrayList.remove(position);
                        for (int i=position; i<mMediaFileEntityArrayList.size(); i++) {
                            mMediaFileEntityArrayList.get(i).setScheduleIndex(i);
                        }
                        updateMediaFilesList();
                    }
                });
            }
        });

        return listItemView;
    }

    private void updateMediaFilesList() {
        notifyDataSetChanged();
        MediaFileEntity[] mediaFileEntities = new MediaFileEntity[mMediaFileEntityArrayList.size()];
        mDataRepository.insertMediaFile(mMediaFileEntityArrayList.toArray(mediaFileEntities), new DataRepository.OnDatabaseWriteComplete() {
            @Override
            public void onComplete() {
                return;
            }
        });
    }
}
