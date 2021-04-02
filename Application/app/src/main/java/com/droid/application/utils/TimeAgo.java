package com.droid.application.utils;

public class TimeAgo {
    public static String getTimeAgo(long time) {
        long result;
        if((result = time / (3600 * 365 * 24)) > 0 ) {
            if(result > 1) {
                return result + " yrs ago";
            }
            return result + " yr ago";
        } else if((result = time / (3600 * 30 * 24)) > 0 ) {
            if(result > 1) {
                return result + " months ago";
            }
            return result + " month ago";
        } else if((result = time / (3600 * 7 * 24)) > 0 ) {
            if(result > 1) {
                return result + " week ago";
            }
            return result + " weeks ago";
        } else if((result = time / (3600 * 24)) > 0 ) {
            if(result > 1) {
                return result + " days ago";
            }
            return result + " day ago";
        } else if((result = time / (3600)) > 0 ) {
            if(result > 1) {
                return result + " hours ago";
            }
            return result + " hour ago";
        } else if((result = time / 60) > 0 ) {
            if(result > 1) {
                return result + " mins ago";
            }
            return result + " min ago";
        } else {
            return "NA";
        }
    }
}
