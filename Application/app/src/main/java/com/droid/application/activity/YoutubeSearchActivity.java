package com.droid.application.activity;

import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import android.provider.SearchRecentSuggestions;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.droid.application.R;
import com.droid.application.adapters.DashboardAdapter;
import com.droid.application.api.VolleyResponseListner;
import com.droid.application.config.Config;
import com.droid.application.config.EndPoints;
import com.droid.application.config.JsonKeys;
import com.droid.application.data.VideoData;
import com.droid.application.provider.SuggestionProvider;
import com.droid.application.utils.EndlessRecyclerOnScrollListner;
import com.droid.application.utils.NetworkChangeReceiver;
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

public class YoutubeSearchActivity extends AppCompatActivity {
    public final String TAG =YoutubeSearchActivity.class.getName();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private DashboardAdapter adapter;
    static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    static NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
    private List<VideoData> videoList = new ArrayList<>();
    private String nextPageToken = null;
    private String query = null;
    private SearchView searchView;
    private LoadingView mLoadingLayout;
    private int count = 0;
    
    Integer shortAnimDuration;
    
    IntentFilter filter = new IntentFilter(ACTION);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        Toolbar mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle(getString(R.string.popular_videos));
        setSupportActionBar(mToolbar);
        
        this.registerReceiver(networkChangeReceiver, filter);
       
        mLoadingLayout = (LoadingView)findViewById(R.id.loading); 
        shortAnimDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = (RecyclerView)findViewById(R.id.searchRecycleList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(View view, int position) {
                                                        VideoData intentData = videoList.get(position);
                                                        Intent videoDetailsIntent = new Intent(getApplicationContext(), YoutubeDetailsActivity.class);
                                                        videoDetailsIntent.putExtra("ob", intentData);
                                                        startActivity(videoDetailsIntent);
                                                    }
                                                }));
        videoList = new ArrayList<VideoData>();
        
        adapter = new DashboardAdapter(getApplicationContext(), videoList);
        recyclerView.setAdapter(adapter);
        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_application, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                @Override
                public boolean onSuggestionSelect(int position) {
                    return true;
                }

                @Override
                public boolean onSuggestionClick(int position) {
                    CursorAdapter selectedView = searchView.getSuggestionsAdapter();
                    Cursor cursor = (Cursor) selectedView.getItem(position);
                    int index = cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1);
                    searchView.setQuery(cursor.getString(index), true);
                    return true;
                }
            });
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(networkChangeReceiver, filter);
//        count++;
//        Log.i(TAG, "onResume: " + count);
//        if(videoList.size() == 0 && count > 1) {
//            Toast.makeText(getApplicationContext(), "Resume", Toast.LENGTH_LONG).show();
//            getData(query, null);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        adapter.removeAll();
        adapter.notifyDataSetChanged();
        setIntent(intent);
        handleIntent(intent);
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

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            nextPageToken = null;
            query = intent.getStringExtra(SearchManager.QUERY);
            getSupportActionBar().setTitle(query);
            recyclerView.setOnScrollListener(null);
            setScrollListener();
            getData(query, nextPageToken);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
        }
    }

    public void getData(String q, String nextToken) {
        new getVideoId(q, nextToken).execute();
    }

    private class getVideoId extends AsyncTask<String, Float, List<String>> {

        String q;
        String nextToken;
        public getVideoId(String q, String nextToken) {
            this.q = q;
            this.nextToken = nextToken;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            
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
        protected List<String> doInBackground(String... params) {
            // TODO Auto-generated method stub
            List<String> videoIdList= new ArrayList<String>();

            try {
                String URL = EndPoints.SEARCH_VIDEO_URL;
                if (nextToken != null) {
                    URL = URL + "&pageToken=" + nextToken;
                }
                URL = URL + "&q=" + q;

                try {
                    String response = getUrlString(URL);
                    JSONObject jsonObject = new JSONObject(response.toString());
                    nextPageToken = jsonObject.getString(JsonKeys.NEXT_PAGE_TOKEN);
                    JSONArray items = jsonObject.getJSONArray(JsonKeys.ITEMS);
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject idObject = items.getJSONObject(i).getJSONObject(JsonKeys.ID);
                        if (idObject.getString(JsonKeys.KIND).equals(JsonKeys.KIND_VIDEO)) {
                            String videoId = idObject.getString(JsonKeys.VIDEO_ID);
                            videoIdList.add(videoId);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // TODO: 9/3/16 Handle Error
                    Toast.makeText(getApplicationContext(), "Some Error", Toast.LENGTH_LONG).show();
                    
                }

            } catch (Exception e1) {
                e1.printStackTrace();
            }

            return videoIdList;

        }

        @Override
        protected void onPostExecute(List<String> result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (result.size() < 1) {
                
            } else {
              
                String videoIdsForDetail = TextUtils.join(",", result);
                getVideoDetails(videoIdsForDetail);
            }
        }
    }


    public void getVideoDetails(String Ids) {
        new VideoDetails(Ids).execute();
    }

    private class VideoDetails extends AsyncTask<String, Float, List<VideoData>> {

        String Ids;
        public VideoDetails(String Ids) {
            this.Ids = Ids;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            crossFade(findViewById(R.id.searchRecycleList), findViewById(R.id.loading));
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
        protected List<VideoData> doInBackground(String... params) {
            // TODO Auto-generated method stub
            List<VideoData> tempVideoList= new ArrayList<VideoData>();

            try {
                String  URL =  EndPoints.VIDEO_DETAILS_URL + "&id=" + Ids;

                try {
                    String response = getUrlString(URL);
                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONArray items = jsonObject.getJSONArray(JsonKeys.ITEMS);
                    for (int i = 0; i < items.length(); i++) {
                        VideoData video = new VideoData();
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
                        tempVideoList.add(video);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // TODO: 9/3/16 Handle Error
                    Toast.makeText(getApplicationContext(), "Some Error", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e1) {
                e1.printStackTrace();
            }

            return videoList;

        }

        @Override
        protected void onPostExecute(List<VideoData> result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (result.size() < 1) {
                crossFade(findViewById(R.id.searchRecycleList), findViewById(R.id.loading));
            } else {
                //adapter.addAll(result);
                crossFade(findViewById(R.id.loading), findViewById(R.id.searchRecycleList));
                
                adapter.addAll(result);
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void setScrollListener() {
        recyclerView.setOnScrollListener(new EndlessRecyclerOnScrollListner(layoutManager) {
                @Override
                public void onLoadMore(int current_page) {
                    Log.i(TAG, "onLoadMore: " + current_page);
                    getData(query, nextPageToken);
                }
            });
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
    
    /*public void searchRetry(View view) {
        recyclerView.setVisibility(View.VISIBLE);
        getData(query, null);
    }*/
    
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
