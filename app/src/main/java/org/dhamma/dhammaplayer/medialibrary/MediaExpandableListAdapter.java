package org.dhamma.dhammaplayer.medialibrary;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.dhamma.dhammaplayer.R;
import org.dhamma.dhammaplayer.media.MediaPlayer;

import java.util.ArrayList;

public class MediaExpandableListAdapter extends BaseExpandableListAdapter {
    private ArrayList<MediaGroup> mMediaGroupArrayList;
    private Context mContext;

    public MediaExpandableListAdapter(ArrayList<MediaGroup> mediaGroupArrayList, Context context) {
        mMediaGroupArrayList = mediaGroupArrayList;
        mContext = context;
    }

    @Override
    public int getGroupCount() {
        return mMediaGroupArrayList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mMediaGroupArrayList.get(groupPosition).getMediaList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mMediaGroupArrayList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mMediaGroupArrayList.get(groupPosition).getMediaList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition*mMediaGroupArrayList.size()+childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        MediaGroup mediaGroup = mMediaGroupArrayList.get(groupPosition);
        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.media_group_item, parent, false);
        }
        TextView tv1 = convertView.findViewById(R.id.tvTimeStamp);
        tv1.setText(mediaGroup.getTimeStamp());
        TextView tv2 = convertView.findViewById(R.id.tvMediaLabel);
        tv2.setText(mediaGroup.getScheduleLabel());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final MediaGroup mediaGroup = mMediaGroupArrayList.get(groupPosition);
        final String mediaPath = mediaGroup.getMediaList().get(childPosition).getMediaPath();
        final String mediaTitle = mediaGroup.getMediaList().get(childPosition).getMediaTitle();
        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.media_child_item, parent, false);
        }
        TextView tv = convertView.findViewById(R.id.tvMediaTitle);
        tv.setText(mediaTitle);
        ImageButton ib = convertView.findViewById(R.id.btPlayPause);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Play media file onclick.
                Intent intent = new Intent(mContext, MediaPlayer.class);
                intent.putExtra(MediaPlayer.KEY_MEDIA_TYPE, mediaGroup.getMediaType());
                intent.putExtra(MediaPlayer.KEY_MEDIA_PATH, mediaPath);
                intent.putExtra(MediaPlayer.KEY_MEDIA_TITLE, mediaTitle);
                mContext.startActivity(intent);
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
