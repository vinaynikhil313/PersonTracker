package com.pabba.persontracker.utils;

import android.util.Log;

import com.loopj.android.http.*;

/**
 * Created by Vinay Nikhil Pabba on 16-12-2015.
 */

public class WSConnection {
    private static final String BASE_URL = "http://ec2-52-90-110-157.compute-1.amazonaws.com:8080/PersonTrackerServer";
    //private static final String BASE_URL = "http://10.0.2.2:8080/PersonTrackerServer";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, AsyncHttpResponseHandler responseHandler) {
        Log.i ("WSConnection", getAbsoluteUrl (url));
        client.get(getAbsoluteUrl (url), responseHandler);
    }

    public static void post(String url, RequestParams requestParams, AsyncHttpResponseHandler responseHandler){
        Log.i("WSConnection", getAbsoluteUrl (url) + requestParams.toString ());
        client.post (getAbsoluteUrl (url), requestParams, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

}
