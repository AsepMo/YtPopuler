package com.droid.application.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.droid.application.utils.NetworkUtil;
import com.droid.application.R;

public class NoInternetActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);

    }
    
    public void retry(View view) {
        int status = NetworkUtil.getConnectivityStatusString(getApplicationContext());
        if(status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
            Toast.makeText(getApplicationContext(), "No Internet", Toast.LENGTH_LONG).show();
        } else {
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(getApplicationContext(), "No Internet", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
