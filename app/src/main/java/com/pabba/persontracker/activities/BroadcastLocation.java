package com.pabba.persontracker.activities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.pabba.persontracker.R;
import com.pabba.persontracker.utils.PubnubTool;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Vinay Nikhil Pabba on 01-01-2016.
 */
public class BroadcastLocation extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private boolean broadcastLocation = true;

    private TextView displayLocation;
    private ProgressBar progressBar;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private static PolylineOptions polylineOptions;
    private static GoogleMap googleMap;

    private static final String TAG = BroadcastLocation.class.getSimpleName ();

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.broadcast_location);
        getActionBar ().setDisplayHomeAsUpEnabled (true);

        displayLocation = (TextView) findViewById (R.id.location_text);
        progressBar = (ProgressBar) findViewById (R.id.progress_bar);
        progressBar.setVisibility (View.VISIBLE);

        googleApiClient = new GoogleApiClient.Builder (this)
                .addConnectionCallbacks (this)
                .addApi (LocationServices.API)
                .addOnConnectionFailedListener (this)
                .build ();

        googleApiClient.connect ();

        locationRequest = new LocationRequest ();
        locationRequest.setPriority (LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval (10 * 1000);
        locationRequest.setFastestInterval (2 * 1000);

    }

    @Override
    protected void onResume () {
        super.onResume ();
        if(!googleApiClient.isConnected ()) {
            googleApiClient.connect ();
            setUpMap ();
        }
    }

    private void setUpMap () {

        googleMap = ((SupportMapFragment) getSupportFragmentManager ()
                .findFragmentById (R.id.broadcast_map)).getMap ();

        googleMap.getUiSettings ().setZoomControlsEnabled (true);
        googleMap.getUiSettings ().setZoomGesturesEnabled (true);

        polylineOptions = new PolylineOptions ();
        polylineOptions.color(Color.BLUE).width (10);

    }

    @Override
    protected void onPause () {
        super.onPause ();
        if (googleApiClient.isConnected ()) {
            LocationServices.FusedLocationApi.removeLocationUpdates (googleApiClient, this);
            googleApiClient.disconnect ();
        }
        finish ();
    }

    private static final int REQUEST_CODE_LOCATION = 2;

    @Override
    public void onConnected (Bundle bundle) {

        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions (this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION);
        } else {
            Location location = LocationServices.FusedLocationApi.getLastLocation (googleApiClient);
            if(location != null)
                sendLocation (location);
            LocationServices.FusedLocationApi.requestLocationUpdates (googleApiClient, locationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended (int i) {

    }

    @Override
    public void onConnectionFailed (ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged (Location location) {
        if(!broadcastLocation)
            return;
        Log.i (TAG, location.getLatitude () + ", " + location.getLongitude ());
        sendLocation (location);
    }

    private void sendLocation(Location location){
        displayLocation.setText ("Your current location is :\n" + location.getLatitude () + ", " + location.getLongitude ());
        JSONObject jsonObject = new JSONObject ();
        try {
            jsonObject.put ("lat", location.getLatitude ());
            jsonObject.put ("lng", location.getLongitude ());
        } catch (JSONException e) {
            e.printStackTrace ();
        }

        PubnubTool.publish (jsonObject);
        LatLng latLng = new LatLng(location.getLatitude (), location.getLongitude ());
        updatePolyline (latLng);
        updateMarker (latLng);
        updateCamera (latLng);
    }

    public void stopBroadcast(View view){
        LocationServices.FusedLocationApi.removeLocationUpdates (googleApiClient, this);
        broadcastLocation = false;
        progressBar.setVisibility (View.INVISIBLE);
    }

    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Location location = LocationServices.FusedLocationApi.getLastLocation (googleApiClient);
                if(location != null)
                    sendLocation (location);
                LocationServices.FusedLocationApi.requestLocationUpdates (googleApiClient, locationRequest, this);
            } else {
                Toast.makeText (BroadcastLocation.this, "Unable to get Location Permissions", Toast.LENGTH_LONG).show ();
            }
        }
    }

    private void updatePolyline(LatLng latLng) {
        googleMap.clear ();
        googleMap.addPolyline (polylineOptions.add (latLng));
    }

    private void updateMarker(LatLng latLng) {
        googleMap.addMarker (new MarkerOptions ().position (latLng));
    }

    private void updateCamera(LatLng latLng) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom (latLng, 16));
    }
}
