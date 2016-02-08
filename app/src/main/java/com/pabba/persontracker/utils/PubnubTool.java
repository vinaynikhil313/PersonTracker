package com.pabba.persontracker.utils;

import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONObject;

/**
 * Created by Vinay Nikhil Pabba on 01-01-2016.
 */
public class PubnubTool {

    private static Pubnub pubnub;
    private static final String PUBLISH_KEY = "pub-c-f832d610-0531-494c-895d-7a4d24f92758";
    private static final String SUBSCRIBE_KEY = "sub-c-1005e6ac-b09c-11e5-8d24-0619f8945a4f";
    private static String MY_CHANNEL = null;
    private static final String AUTH_KEY = "123456";

    private static final String TAG = PubnubTool.class.getSimpleName ();

    private static PubnubTool pubnubTool = new PubnubTool ();

    private PubnubTool(){
        pubnub = new Pubnub(PUBLISH_KEY, SUBSCRIBE_KEY);
        Log.i (TAG, "Pubnub created");
    }

    public static void setMyChannel(String channel){
        MY_CHANNEL = channel;
    }

    public static PubnubTool getInstance(){
        if(pubnubTool == null){
            pubnubTool = new PubnubTool ();
        }
        return pubnubTool;
    }

    public static void subscribe(String channel, String authKey, Callback callback){

        try {
            pubnub.setAuthKey (authKey);
            Log.i (TAG, "Auth key is -------------- " + pubnub.getAuthKey ());
            pubnub.subscribe (channel, callback);
        } catch (PubnubException e) {
            Log.e(TAG, "Error while subscribing to channel");
        }

    }

    public static void unsubscribe(String channel){
        pubnub.unsubscribe (channel);
        Log.i(TAG, "Channel Unsubscribed");
    }

    public static void publish(JSONObject message){
        pubnub.setAuthKey (AUTH_KEY);
        pubnub.publish (MY_CHANNEL, message, callback);
    }

    private static Callback callback = new Callback () {
        @Override
        public void successCallback (String s, Object o) {
            super.successCallback (s, o);
            Log.i(TAG, "Publish Successful");
        }

        @Override
        public void errorCallback (String s, PubnubError pubnubError) {
            super.errorCallback (s, pubnubError);
            Log.e(TAG, "Publish error number " + pubnubError);
        }
    };

}
