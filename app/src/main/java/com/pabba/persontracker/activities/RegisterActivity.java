package com.pabba.persontracker.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pabba.persontracker.R;
import com.pabba.persontracker.utils.Constants;
import com.pabba.persontracker.utils.PubnubTool;
import com.pabba.persontracker.utils.WSConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Vinay Nikhil Pabba on 09-01-2016.
 */
public class RegisterActivity extends Activity {

    private EditText userName;
    private EditText email;
    private EditText password;
    private EditText authKey;
    private Button generate;
    private Button register;

    private static final String TAG = RegisterActivity.class.getSimpleName ();

    private static SharedPreferences settings;
    private static SharedPreferences.Editor editor;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.register);

        settings = this.getSharedPreferences(Constants.MY_PREFERENCES, Context.MODE_PRIVATE);
        editor = settings.edit();

        userName = (EditText) findViewById (R.id.user_name_register);
        email = (EditText) findViewById (R.id.email_register);
        password = (EditText) findViewById (R.id.password_register);
        authKey = (EditText) findViewById (R.id.auth_key_register);
        generate = (Button) findViewById (R.id.button_generate);
        register = (Button) findViewById (R.id.button_register);

        generate.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                generateAuthKey();
            }
        });
        register.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick (View v) {
                if(validate ())
                    registerUser ();
            }
        });

    }

    private void generateAuthKey () {

        String s = "";
        Random r = new Random ();
        for(int i=0;i<6;i++) {
            s += r.nextInt (10);
        }
        authKey.setText (s);
    }

    private void registerUser () {

        String url = "/register";

        RequestParams requestParams = new RequestParams ();
        requestParams.put("user_name", userName.getText ().toString ());
        requestParams.put("email", email.getText ().toString ());
        requestParams.put ("password", password.getText ().toString ());
        requestParams.put ("auth_key", authKey.getText ().toString ());

        WSConnection.post (url, requestParams, new WSConnectionResponseHandler ());

    }

    public boolean validate() {

        boolean valid = true;

        String userNameText = userName.getText ().toString ();
        String emailText = email.getText().toString();
        String passwordText = password.getText().toString();
        String authKeyText = authKey.getText ().toString ();

        if(userNameText.isEmpty () || userNameText.length () <= 3 || userNameText.length () > 20){
            userName.setError ("Username should be 4 to 20 characters");
            valid = false;
        }
        else{
            userName.setError (null);
        }

        if (emailText.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            email.setError("Enter a valid email address");
            valid = false;
        }
        else {
            email.setError(null);
        }

        if (passwordText.isEmpty() || passwordText.length() < 4 || passwordText.length() > 20) {
            password.setError("Between 4 and 20 alphanumeric characters");
            valid = false;
        }
        else {
            password.setError(null);
        }

        if(authKeyText.isEmpty () || authKeyText.length () != 6){
            authKey.setError ("Auth key should be 6 digit");
            valid = false;
        }
        else{
            authKey.setError (null);
        }

        return valid;
    }

    private class WSConnectionResponseHandler extends JsonHttpResponseHandler{

        @Override
        public void onFailure (int statusCode, Header[] headers, String responseString, Throwable throwable) {
            super.onFailure (statusCode, headers, responseString, throwable);
            Toast.makeText (RegisterActivity.this, "Connection failed to Server", Toast.LENGTH_LONG).show ();
        }

        @Override
        public void onSuccess (int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess (statusCode, headers, response);
            int result = -1;

            try {
                result = response.getInt ("result");
            } catch (JSONException e) {
                Log.e (TAG, "Bad JSON Response");
            }
            switch (result){

                case Constants.RESULT_SUCCESS:
                    Log.i (TAG, "User registered successfully");
                    PubnubTool.setMyChannel (userName.getText ().toString ());
                    Toast.makeText (RegisterActivity.this, "Welcome to PersonTracker!", Toast.LENGTH_SHORT).show ();

                    editor.putString ("user_name", userName.getText ().toString ());
                    editor.putBoolean ("first_time", false);
                    editor.putInt ("auth_key", Integer.parseInt (authKey.getText ().toString ()));
                    editor.commit ();

                    Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity (i);
                    finish();
                    break;

                case Constants.RESULT_EMAIL_EXISTS:
                    Toast.makeText (RegisterActivity.this, "Email ID already registered. Try logging in", Toast.LENGTH_LONG).show ();
                    break;

                case Constants.RESULT_USERNAME_EXISTS:
                    Toast.makeText (RegisterActivity.this, "Username already used. Pick another one", Toast.LENGTH_LONG).show ();
                    break;

                case Constants.RESULT_USER_ALREADY_REGISTERED:
                    Toast.makeText (RegisterActivity.this, "You have already registered.\nClick on Forgot Password to reset password", Toast.LENGTH_LONG).show ();
                    break;

                case Constants.RESULT_UNKNOWN_DB_EXCEPTION:
                    Toast.makeText (RegisterActivity.this, "Server Exception. Try again later", Toast.LENGTH_LONG).show ();
                    break;

                default:
                    Log.e(TAG, "JSON Response = " + result);
                    break;

            }
        }
    }

}
