package com.droid.application.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import com.droid.application.data.VideoData;
import com.droid.application.R;

import java.util.List;
import com.bumptech.glide.Glide;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {
    private Context context;
    List<VideoData> videoList;


    public DashboardAdapter(Context context, List<VideoData> videoList) {
        super();
        this.context = context;
        this.videoList = videoList;
    }

    @Override
    public DashboardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_row_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DashboardAdapter.ViewHolder holder, int position) {
        VideoData video = videoList.get(position);
        Glide.with(context).load(video.getMediumThumbnail()).placeholder(R.drawable.video_placeholder).into(holder.imageView);
        Glide.with(context).load(video.getMediumThumbnail()).placeholder(R.drawable.video_placeholder).into(holder.thumbs);   
        holder.uploaderName.setText(video.getChannelTitle());
        holder.videoName.setText(video.getVideoTitle());
        holder.views.setText(video.getViewCount() + " views");
        holder.duration.setText(video.getDuration());
        holder.timeAgo.setText(video.getTimeAgo());
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }
    public void addAll(List<VideoData> newVideoList) {
        videoList.addAll(newVideoList);
    }
    public void removeAll() {
        videoList.clear();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public ImageView thumbs;
        public TextView videoName;
        public TextView uploaderName;
        public TextView views;
        public TextView duration;
        public TextView timeAgo;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.videoListThumbnail);
            thumbs = (ImageView)itemView.findViewById(R.id.thumbs);
            
            videoName = (TextView)itemView.findViewById(R.id.videoListName);
            uploaderName = (TextView)itemView.findViewById(R.id.videoListUploaderName);
            timeAgo = (TextView)itemView.findViewById(R.id.videoListPublishedAt);
            views = (TextView)itemView.findViewById(R.id.videoListViews);
            duration = (TextView)itemView.findViewById(R.id.videoListDuration);
        }
    }
}
