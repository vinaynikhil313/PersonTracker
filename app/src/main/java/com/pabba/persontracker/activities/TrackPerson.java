package com.pabba.persontracker.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.pabba.persontracker.R;
import com.pabba.persontracker.utils.PubnubTool;
import com.pubnub.api.Callback;
import com.pubnub.api.PubnubError;

import org.json.JSONException;
import org.json.JSONObject;


public class TrackPerson extends FragmentActivity {

    private static GoogleMap googleMap;

    private Callback callback = new Callback () {
        @Override
        public void successCallback (String channel, Object message) {
            super.successCallback (channel, message);
            Log.d ("PubnubCallback", "Message Received: " + message.toString ());

            JSONObject jsonMessage = (JSONObject) message;
            try {
                double lat = jsonMessage.getDouble("lat");
                double lng = jsonMessage.getDouble("lng");
                latLng = new LatLng (lat, lng);
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }

            runOnUiThread (new Runnable () {
                @Override
                public void run () {
                    updatePolyline ();
                    updateCamera ();
                    updateMarker ();
                }
            });
        }

        @Override
        public void errorCallback (String s, PubnubError pubnubError) {
            super.errorCallback (s, pubnubError);
            Log.e(TAG, pubnubError.toString ());
        }
    };
    private String channelName;
    private String authKey;

    private static PolylineOptions polylineOptions;

    private static final String TAG = TrackPerson.class.getSimpleName ();

    public TrackPerson () {
        super ();
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.track_person);
        getActionBar ().setDisplayHomeAsUpEnabled (true);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            channelName = extras.getString("user_name");
            authKey = extras.getString ("auth_key");
        }
        Log.i(TAG, "Channel name entered is " + channelName);
    }

    @Override
    protected void onPause () {
        super.onPause ();
        PubnubTool.unsubscribe (channelName);
        finish ();
    }

    @Override
    protected void onResume () {
        super.onResume ();
        setUpMap ();
    }

    private void setUpMap () {

        googleMap = ((SupportMapFragment) getSupportFragmentManager ()
                .findFragmentById (R.id.map)).getMap ();

        googleMap.getUiSettings ().setZoomControlsEnabled (true);
        googleMap.getUiSettings ().setZoomGesturesEnabled (true);

        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLUE).width(10);

        PubnubTool.subscribe (channelName, authKey, callback);
    }

    LatLng latLng;

    private void updatePolyline() {
        googleMap.clear();
        googleMap.addPolyline(polylineOptions.add(latLng));
    }

    private void updateMarker() {
        googleMap.addMarker (new MarkerOptions ().position (latLng));
    }

    private void updateCamera() {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom (latLng, 16));
    }

}
