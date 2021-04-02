package com.droid.application.activity;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.droid.application.R;
import com.droid.application.fragments.DashboardFragment;
import com.droid.application.utils.NetworkChangeReceiver;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getName();
    
    private SearchView searchView;
    static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    static NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
    IntentFilter filter = new IntentFilter(ACTION);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_dashboard);
       
        Toolbar mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle(getString(R.string.popular_videos));
        setSupportActionBar(mToolbar);
        
        this.registerReceiver(networkChangeReceiver, filter);
        showFragment(new DashboardFragment());
    }
    
    public void showFragment(Fragment fragment)
    {
        getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.content_frame, fragment)
        .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_application, menu);
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
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
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }
}
