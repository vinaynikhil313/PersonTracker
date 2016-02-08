package com.pabba.persontracker.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.pabba.persontracker.R;
import com.pabba.persontracker.utils.Constants;
import com.pabba.persontracker.utils.PubnubTool;
import com.pabba.persontracker.utils.WSConnection;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Vinay Nikhil Pabba on 03-01-2016.
 */
public class LoginActivity extends Activity {

    private EditText userNameField;
    private EditText passwordField;
    private TextView registerText;

    private static SharedPreferences settings;
    private static SharedPreferences.Editor editor;
    private static final String TAG = LoginActivity.class.getSimpleName ();

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.login);

        userNameField = (EditText) findViewById (R.id.user_name);
        passwordField = (EditText) findViewById (R.id.password);
        registerText = (TextView) findViewById (R.id.text_register);

        registerText.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity (i);
                finish ();
            }
        });

        settings = this.getSharedPreferences (Constants.MY_PREFERENCES, Context.MODE_PRIVATE);
        editor = settings.edit ();

        editor.putBoolean ("first_time", true);
        editor.putString ("user_name", "");
        editor.commit ();

    }

    private String userName;

    public void register (View view) {
        userName = this.userNameField.getText ().toString ();
        if (userName.length () < 3) {
            Toast.makeText (LoginActivity.this, "Please enter a username more than 3 characters", Toast.LENGTH_LONG).show ();
            return;
        }

        String password = passwordField.getText ().toString ();

        String url = "/login?"
                + "user_name=" + userName
                + "&password=" + password;

        WSConnection.get (url, new WSConnectionResponseHandler ());
    }

    private class WSConnectionResponseHandler extends JsonHttpResponseHandler {

        @Override
        public void onSuccess (int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess (statusCode, headers, response);
            Log.i (TAG, response.toString ());
            try {
                if (response.getInt ("result") == 1) {
                    Toast.makeText (LoginActivity.this, "Welcome " + userName, Toast.LENGTH_LONG).show ();
                    editor.putString ("user_name", userName);
                    editor.putBoolean ("first_time", false);
                    editor.putInt ("auth_key", response.getInt ("auth_key"));
                    editor.commit ();
                    PubnubTool.setMyChannel (userName);
                    Intent i = new Intent (LoginActivity.this, MainActivity.class);
                    startActivity (i);
                    finish ();
                } else if(response.getInt("result") == 0) {
                    Toast.makeText (LoginActivity.this, "Incorrect username!\n" +
                            "Please correct it or register now if you don't have an account!", Toast.LENGTH_LONG).show ();
                } else if(response.getInt ("result") == -1){
                    Toast.makeText (LoginActivity.this, "Incorrect password. Please try again", Toast.LENGTH_LONG).show ();
                }
            } catch (JSONException e) {
                e.printStackTrace ();
            }
        }

        @Override
        public void onFailure (int statusCode, Header[] headers, String responseString, Throwable throwable) {
            super.onFailure (statusCode, headers, responseString, throwable);
            Log.e (TAG, responseString);
        }

    }
}
