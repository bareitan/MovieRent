package com.example.bareitan.movierent;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;


public class RegisterActivity extends AppCompatActivity {
    public static final String PREFS_LOGIN = "LoginPrefsFile";
    public static final String PREFS_ADMIN = "AdminPrefsFile";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_REMEMBER = "remember";
    private static final String PREF_USER_ID = "userid";
    private static final String PREF_IS_ADMIN = "isAdmin";
    String RENT_WS;
    private UserRegisterTask mRegisterTask = null;
    private EditText mEmailTV;
    private EditText mPasswordTV;
    private EditText mFirstNameTV;
    private EditText mLastNameTV;
    private View mProgressView;
    private View mRegisterFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        RENT_WS = sharedPref.getString("ws_uri", "");
        mEmailTV = (EditText) findViewById(R.id.email);
        mPasswordTV = (EditText) findViewById(R.id.password);
        mFirstNameTV = (EditText) findViewById(R.id.first_name);
        mLastNameTV = (EditText) findViewById(R.id.last_name);


        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);
    }

    private void attemptRegister() {
        if(mRegisterTask != null) {
            return;
        }

        mFirstNameTV.setError(null);
        mLastNameTV.setError(null);
        mEmailTV.setError(null);
        mPasswordTV.setError(null);


        String email = mEmailTV.getText().toString();
        String password = mPasswordTV.getText().toString();
        String firstName = mFirstNameTV.getText().toString();
        String lastName = mLastNameTV.getText().toString();

        boolean cancel = false;
        View focusView = null;



        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordTV.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordTV;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailTV.setError(getString(R.string.error_field_required));
            focusView = mEmailTV;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailTV.setError(getString(R.string.error_invalid_email));
            focusView = mEmailTV;
            cancel = true;
        }

        if (TextUtils.isEmpty(firstName)) {
            mFirstNameTV.setError(getString(R.string.error_field_required));
            focusView = mFirstNameTV;
            cancel = true;
        }

        if (TextUtils.isEmpty(lastName)) {
            mLastNameTV.setError(getString(R.string.error_field_required));
            focusView = mLastNameTV;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mRegisterTask = new RegisterActivity.UserRegisterTask(email, password, firstName, lastName);
            mRegisterTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class UserRegisterTask extends AsyncTask<Void,Void,Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String mFirstName;
        private final String mLastName;
        Boolean registeredSuccessfully;
        String error;

        UserRegisterTask(String email, String password, String firstName, String lastName) {
            mEmail = email;
            mPassword = password;
            mFirstName = firstName;
            mLastName = lastName;
            registeredSuccessfully = false;
            error ="";
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mRegisterTask = null;
            showProgress(false);

            if(success) {
                getSharedPreferences(PREFS_LOGIN, MODE_PRIVATE)
                        .edit()
                        .putString(PREF_EMAIL, mEmail)
                        .putString(PREF_PASSWORD, mPassword)
                        .remove(PREF_USER_ID)
                        .putBoolean(PREF_IS_ADMIN, false)
                        .putBoolean(PREF_REMEMBER, false)
                        .commit();
                Intent loginIntent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(loginIntent);
            } else {
                Toast.makeText(RegisterActivity.this, "Error occured", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            mRegisterTask = null;
            showProgress(false);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String REGISTER_WS = getString(R.string.register_ws);

            Uri builtUri = Uri.parse(RENT_WS).buildUpon()
                    .appendEncodedPath(REGISTER_WS)
                    .appendQueryParameter("email", mEmail)
                    .appendQueryParameter("password", mPassword)
                    .appendQueryParameter("firstName", mFirstName)
                    .appendQueryParameter("lastName", mLastName)
                    .appendQueryParameter("isAdmin", "0")
                    .build();

            URL url = null;
            HttpURLConnection urlConnection =null;
            try {
                url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    response.append(line);
                }
                JSONObject responseJSON = new JSONObject(response.toString());
                registeredSuccessfully = responseJSON.optBoolean("registeredSuccessfully");
                error = responseJSON.optString("error");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }finally {
                urlConnection.disconnect();
            }

            return registeredSuccessfully;
        }
    }
}
