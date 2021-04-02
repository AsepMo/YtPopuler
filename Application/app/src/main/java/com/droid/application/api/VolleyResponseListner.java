package com.droid.application.api;

public interface VolleyResponseListner {
    void onError(String message);

    void onResponse(Object response);
}
