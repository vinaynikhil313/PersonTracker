package com.pabba.persontracker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.pabba.persontracker.R;
import com.pabba.persontracker.utils.WSConnection;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends Activity{

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate (R.menu.main_activity_menus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_menu_button:
                Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity (i);
                finish();
                break;

            default:
                break;
        }

        return true;
    }


    public void openBroadcasting(View view){
        if(locationServicesEnabled ()) {
            Intent i = new Intent (MainActivity.this, BroadcastLocation.class);
            startActivity (i);
        }
    }

    private boolean locationServicesEnabled () {

        LocationManager lm = (LocationManager)getSystemService (Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Do you want to turn on Location Services?");
            dialog.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity (myIntent);
                }
            });
            dialog.setNegativeButton("Nope", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Toast.makeText (MainActivity.this, "You cannot broadcast your location\nwithout turning on Location Servies", Toast.LENGTH_LONG).show ();

                }
            });
            dialog.show();
        }
        else{
            return true;
        }
        return false;
    }

    private View createView(){
        LayoutInflater li = LayoutInflater.from (MainActivity.this);
        View v = li.inflate (R.layout.tracking_details, null, false);
        return v;
    }

    public void openTrackingDialog (View view) {

        View v = createView ();
        final EditText userlName = (EditText) v.findViewById (R.id.tracking_username);
        final EditText authKey = (EditText) v.findViewById (R.id.tracking_auth_key);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder (MainActivity.this);
        alertDialog.setTitle ("Track Location");
        alertDialog.setView (v);
        alertDialog.setPositiveButton ("Track!", new DialogInterface.OnClickListener () {
            @Override
            public void onClick (DialogInterface dialog, int which) {
                /**/
                String userNameText = userlName.getText ().toString ();
                String authKeyText = authKey.getText ().toString ();
                verifyDetails(userNameText, authKeyText);

            }
        });
        alertDialog.setNegativeButton ("Cancel", new DialogInterface.OnClickListener () {
            @Override
            public void onClick (DialogInterface dialog, int which) {

            }
        });

        alertDialog.show ();

    }

    private void verifyDetails (String userNameText, String authKeyText) {

        String url = "/verify"
                + "?user_name="
                + userNameText
                + "&auth_key="
                + authKeyText;

        WSConnection.get (url, new WSConnectionResponseHandler(userNameText, authKeyText));

    }

    private class WSConnectionResponseHandler extends JsonHttpResponseHandler {

        private String userName;
        private String authKey;
        WSConnectionResponseHandler(String userName, String authKey){
            this.userName = userName;
            this.authKey = authKey;
        }

        @Override
        public void onSuccess (int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess (statusCode, headers, response);
            int result = -1;

            try {
                result = response.getInt ("result");
            } catch (JSONException e) {
                e.printStackTrace ();
            }

            if(result == 1){
                Intent i = new Intent (MainActivity.this, TrackPerson.class);
                i.putExtra ("user_name", userName);
                i.putExtra ("auth_key", authKey);
                startActivity (i);
            }
            else if(result == 0){
                Toast.makeText (MainActivity.this, "Incorrect username/auth key. Please verify", Toast.LENGTH_LONG).show ();
            }

        }

        @Override
        public void onFailure (int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            super.onFailure (statusCode, headers, throwable, errorResponse);
            Toast.makeText (MainActivity.this, "Server Error!", Toast.LENGTH_SHORT).show ();
        }
    }
}
