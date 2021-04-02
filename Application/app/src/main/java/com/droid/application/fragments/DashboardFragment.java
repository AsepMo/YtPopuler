package com.droid.application.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.droid.application.R;
import com.droid.application.activity.YoutubeDetailsActivity;
import com.droid.application.adapters.DashboardAdapter;
import com.droid.application.config.Config;
import com.droid.application.config.EndPoints;
import com.droid.application.config.JsonKeys;
import com.droid.application.data.VideoData;
import com.droid.application.utils.EndlessRecyclerOnScrollListner;
import com.droid.application.utils.NetworkChangeReceiver;
import com.droid.application.utils.NetworkUtil;
import com.droid.application.utils.RecyclerItemClickListener;
import com.droid.application.utils.LoadingView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DashboardFragment extends Fragment {

    public static String TAG = DashboardFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private DashboardAdapter adapter;
    private LoadingView mLoadingLayout;
    private List<VideoData> videoList = new ArrayList<>();
    private String nextPageToken = null;
    private int count = 0;
    private View rootView;
    Integer shortAnimDuration;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_application_dashboard, container, false); 
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
     
        mLoadingLayout = (LoadingView)view.findViewById(R.id.loading); 
        shortAnimDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        
        recyclerView = (RecyclerView)view.findViewById(R.id.mainRecycleList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(View view, int position) {
                                                        VideoData intentData = videoList.get(position);
                                                        Intent videoDetailsIntent = new Intent(getActivity(), YoutubeDetailsActivity.class);
                                                        videoDetailsIntent.putExtra("ob", intentData);
                                                        startActivity(videoDetailsIntent);
                                                    }
                                                }));

        recyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListner(layoutManager) {
                @Override
                public void onLoadMore(int current_page) {
                    Log.i(TAG, "onLoadMore: " + current_page);
                    getData();
                }
            });
        videoList = new ArrayList<VideoData>(); 
        adapter = new DashboardAdapter(getActivity(), videoList);
        recyclerView.setAdapter(adapter);
        getData();
    }

    @Override
    public void onResume() {
        super.onResume();
        count++;
        Log.i(TAG, "onResume: " + count);
        if (videoList.size() == 0 && count > 1) {
            getData();
        }
    }

    private void getData() {
        new TheTask().execute();
    }

    private class TheTask extends AsyncTask<Void, Float, Void> {


        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            crossFade(rootView.findViewById(R.id.mainRecycleList), rootView.findViewById(R.id.loading));
        }

        @Override
        protected void onProgressUpdate(Float[] values) {
            // TODO: Implement this method
            super.onProgressUpdate(values);
            /* float progress = values[0];
             progressBar.setMax(100);
             progressBar.setProgress((int) (progress * 100));*/
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            String URL = EndPoints.POPULARVIDEO_URL;
            if (nextPageToken != null) {
                URL = URL + "&pageToken=" + nextPageToken;
            }
            try {
                String response = getUrlString(URL);

                JSONObject json = new JSONObject(response.toString());
                nextPageToken = json.getString(JsonKeys.NEXT_PAGE_TOKEN);
                JSONArray items = json.getJSONArray(JsonKeys.ITEMS);

                for (int i = 0; i < items.length(); i++) {
                    VideoData video = new VideoData();
                    try {
                        JSONObject itemObject = items.getJSONObject(i);
                        video.setVideoId(itemObject.getString(JsonKeys.ID));
                        JSONObject snippet = itemObject.getJSONObject(JsonKeys.SNIPPET);
                        video.setChannelTitle(snippet.getString(JsonKeys.CHANNEL_TITLE));
                        video.setPublishedAt(snippet.getString(JsonKeys.PUBLISHED_AT));
                        video.setChannelId(snippet.getString(JsonKeys.CHANNL_ID));
                        video.setVideoTitle(snippet.getString(JsonKeys.VIDEO_TITLE));
                        video.setDescription(snippet.getString(JsonKeys.DESCRIPTION));
                        JSONObject thumbnails = snippet.getJSONObject(JsonKeys.THUMBNAILS);
                        video.setSmallThumbnail(thumbnails.getJSONObject(JsonKeys.DEFAULT_THUMBNAIL).getString(JsonKeys.URL));
                        video.setMediumThumbnail(thumbnails.getJSONObject(JsonKeys.MEDIUM_THUMBNAIL).getString(JsonKeys.URL));
                        video.setLargeThumbnail(thumbnails.getJSONObject(JsonKeys.HIGH_THUMBNAIL).getString(JsonKeys.URL));
                        JSONObject contentDetails = itemObject.getJSONObject(JsonKeys.CONTENT_DETAILS);
                        video.setDuration(contentDetails.getString(JsonKeys.DURATION));
                        JSONObject statistics = itemObject.getJSONObject(JsonKeys.STATISTICS);
                        video.setViewCount(statistics.getString(JsonKeys.VIEW_COUNT));
                        video.setLikeCount(statistics.getString(JsonKeys.LIKE_COUNT));
                        video.setDislikeCount(statistics.getString(JsonKeys.DISLIKE_COUNT));
                        video.setFavouriteCount(statistics.getString(JsonKeys.FAVORITE_COUNT));
                        video.setCommentCount(statistics.getString(JsonKeys.COMMENT_COUNT));
                        videoList.add(video);
                        
                    } catch (JSONException e) {
                        // TODO: 9/3/16 Handle Error
                        e.printStackTrace();
                    }
                }

            } catch (Exception e1) {
                e1.printStackTrace();
            }


            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            
            adapter.notifyDataSetChanged();
            crossFade(rootView.findViewById(R.id.loading), rootView.findViewById(R.id.mainRecycleList));
        }
    }

    private byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                                      ": with " +
                                      urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    private String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public void mainRetry(View view) {
        recyclerView.setVisibility(View.VISIBLE);
        getData();
    }
 
    private void crossFade(final View toHide, View toShow)
    {

        toShow.setAlpha(0f);
        toShow.setVisibility(View.VISIBLE);

        toShow.animate()
            .alpha(1f)
            .setDuration(shortAnimDuration)
            .setListener(null);

        toHide.animate()
            .alpha(0f)
            .setDuration(shortAnimDuration)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    toHide.setVisibility(View.GONE);
                }
            });
    }
}
