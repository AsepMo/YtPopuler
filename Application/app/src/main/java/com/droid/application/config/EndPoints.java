package com.droid.application.config;

public class EndPoints {
    public static final String POPULARVIDEO_URL = "https://www.googleapis.com/youtube/v3/videos?part=snippet%2CcontentDetails%2Cstatistics&chart=mostPopular&regionCode=ID&maxResults=150&key=" + Config.DEVELOPER_API_KEY;
    public static final String SEARCH_VIDEO_URL = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=150&key=" + Config.DEVELOPER_API_KEY;
    public static final String VIDEO_DETAILS_URL = "https://www.googleapis.com/youtube/v3/videos?part=snippet%2CcontentDetails%2Cstatistics&key=" + Config.DEVELOPER_API_KEY;
    public static final String YOUTUBE_URL_VIDEO = "http://www.youtube.com/watch?v=";
}
