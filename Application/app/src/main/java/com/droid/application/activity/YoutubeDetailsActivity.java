package com.droid.application.activity;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.droid.application.config.Config;
import com.droid.application.config.EndPoints;
import com.droid.application.data.VideoData;
import com.droid.application.R;

import com.bumptech.glide.Glide;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

public class YoutubeDetailsActivity extends AppCompatActivity implements View.OnClickListener
{

    private VideoData videoDetail;
    private int REQ_PLAYER_CODE  = 1;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_video_details);
        Toolbar mToolbar = (Toolbar)findViewById(R.id.toolbar);  
        setSupportActionBar(mToolbar);
         
        try {
            videoDetail = (VideoData)getIntent().getSerializableExtra("ob");
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: 10/3/16 Handle Error
            Toast.makeText(getApplicationContext(), "Some Error", Toast.LENGTH_LONG).show();
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle(videoDetail.getChannelTitle());
        
        ImageView thumbnails = (ImageView)findViewById(R.id.video_thumbnails);
        Glide.with(this).load(videoDetail.getLargeThumbnail()).placeholder(R.drawable.video_placeholder).into(thumbnails);
        
        TextView mDurations = (TextView)findViewById(R.id.detailDuration);
        mDurations.setText(videoDetail.getDuration());
      
        ImageView thumbs = (ImageView)findViewById(R.id.thumbs);
        Glide.with(this).load(videoDetail.getMediumThumbnail()).placeholder(R.drawable.video_placeholder).into(thumbs);

        ((TextView)findViewById(R.id.detailLikes)).setText(videoDetail.getLikeCount());
        ((TextView)findViewById(R.id.detailDislike)).setText(videoDetail.getDislikeCount());
        ((TextView)findViewById(R.id.detailDuration)).setText(videoDetail.getDuration());
        ((TextView)findViewById(R.id.detailViews)).setText(videoDetail.getViewCount() + " views");
        ((TextView)findViewById(R.id.detailTitle)).setText(videoDetail.getVideoTitle());
        ((TextView)findViewById(R.id.detailUploaderName)).setText(videoDetail.getChannelTitle());
        
        TextView mDescriptions = (TextView)findViewById(R.id.detailDescriptions);
        mDescriptions.setText(videoDetail.getDescription());
      
        findViewById(R.id.watch).setOnClickListener(this);
        findViewById(R.id.laters).setOnClickListener(this);
        
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.watch:
                Intent videoIntent = YouTubeStandalonePlayer.createVideoIntent(YoutubeDetailsActivity.this, Config.API_KEY, videoDetail.getVideoId(), 0, true, false);
                startActivityForResult(videoIntent, REQ_PLAYER_CODE);
                break;
            case R.id.laters:
                break; 
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }
    
}
