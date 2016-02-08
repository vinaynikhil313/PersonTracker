package com.pabba.persontracker.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.pabba.persontracker.R;
import com.pabba.persontracker.utils.Constants;
import com.pabba.persontracker.utils.PubnubTool;

/**
 * Created by Vinay Nikhil Pabba on 27-12-2015.
 */
public class SplashScreen extends Activity {

    private static SharedPreferences sharedPreferences;
    //private static SharedPreferences.Editor editor;

    private static final String TAG = SplashScreen.class.getSimpleName ();

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.splash_screen);

        sharedPreferences = getSharedPreferences (Constants.MY_PREFERENCES, Context.MODE_PRIVATE);

    }

    @Override
    protected void onResume () {
        super.onResume ();

        new Handler ().postDelayed (new Runnable () {

            @Override
            public void run () {
                boolean firstTime = sharedPreferences.getBoolean ("first_time", true);
                Log.i (TAG, "First time = " + new Boolean (firstTime).toString ());
                Intent i;
                if (firstTime || sharedPreferences.getString ("user_name", "").equals ("")) {
                    i = new Intent (SplashScreen.this, LoginActivity.class);
                } else {
                    String userName = sharedPreferences.getString ("user_name", "");
                    Toast.makeText (SplashScreen.this, "Welcome back " + userName, Toast.LENGTH_LONG).show ();
                    PubnubTool pubnubTool = PubnubTool.getInstance ();
                    pubnubTool.setMyChannel (userName);
                    i = new Intent (SplashScreen.this, MainActivity.class);
                }
                startActivity (i);
                finish ();
            }

        }, Constants.SPLASH_TIME_OUT);

    }
}
