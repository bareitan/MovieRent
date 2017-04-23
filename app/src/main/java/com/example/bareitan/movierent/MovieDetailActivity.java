package com.example.bareitan.movierent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MovieDetailActivity extends AppCompatActivity {
    public MovieItem movie;
    TextView mMovieNameTV;
    TextView mMovieOverviewTV;
    TextView mMovieCategoryTV;
    ImageView mThumbnail;
    Switch mRentMovieSwitch;
    MovieDeleteTask mDeleteTask;
    RentMovieTask mRentTask;
    ReturnMovieTask mReturnTask;
    boolean mReturnRentError = false;
    IsRentedTask mIsRentedTask;
    int mRentID;
    String RENT_WS;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        String PREFS_ADMIN = "AdminPrefsFile";
        String PREF_IS_ADMIN = "isAdmin";

        SharedPreferences pref = getSharedPreferences(PREFS_ADMIN,MODE_PRIVATE);
        boolean isAdmin = pref.getBoolean(PREF_IS_ADMIN, false);
        if(isAdmin)
            inflater.inflate(R.menu.movie_details_admin_menu, menu);
        inflater.inflate(R.menu.movie_details_user_menu, menu);

        MenuItem actionViewItem = menu.findItem(R.id.rent_movie);

        View v = MenuItemCompat.getActionView(actionViewItem);

        mRentMovieSwitch = (Switch) v.findViewById(R.id.switchCustomAction);
        mRentMovieSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!mReturnRentError) {
                    if (b) {
                        mRentTask = new RentMovieTask();
                        mRentTask.execute(movie.getId().toString());

                    } else {
                        mReturnTask = new ReturnMovieTask();
                        mReturnTask.execute(mRentID);
                    }
                }
                mReturnRentError=false;
            }
        });


        mIsRentedTask = new IsRentedTask();
        mIsRentedTask.execute();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.delete_movie:
                mDeleteTask = new MovieDeleteTask();
                mDeleteTask.execute(movie.getId());
                return true;
            case R.id.edit_movie:
                Intent editMovieIntent = new Intent(this, AddMovieActivity.class);
                editMovieIntent.putExtra("fetched_movie", movie);
                editMovieIntent.putExtra("MODE","EDIT");
                startActivity(editMovieIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        RENT_WS = sharedPref.getString("ws_uri", "");

        mMovieNameTV = (TextView)findViewById(R.id.movie_name);
        mMovieOverviewTV = (TextView)findViewById(R.id.movie_overview);
        mMovieCategoryTV = (TextView)findViewById(R.id.movie_category);
        mThumbnail = (ImageView) findViewById(R.id.thumbnail);
        //mRentMovieSwitch = (Switch) findViewById(R.id.rent_movie);

        Intent caller = getIntent();
        movie = caller.getParcelableExtra("movie");


        mMovieNameTV.setText(movie.getName());
        mMovieOverviewTV.setText(movie.getOverview());
        mMovieCategoryTV.setText(movie.getCategoryName());


        if(!TextUtils.isEmpty(movie.getThumbnail())) {
            Picasso.with(this).load(movie.getThumbnail())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(mThumbnail);
        }


        CommentsFragment commentsFragment = new CommentsFragment();

        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments


        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, commentsFragment).commit();

    }

    public class MovieDeleteTask extends AsyncTask<String, Void, Boolean> {



        @Override
        protected Boolean doInBackground(String... params) {

            String DELETE_WS = getString(R.string.delete_ws);
            Boolean deleted = false;
            try{
                Uri builtUri = Uri.parse(RENT_WS).buildUpon()
                        .appendEncodedPath(DELETE_WS)
                        .appendQueryParameter("movieId", params[0])
                        .build();
                URL url = null;
                HttpURLConnection urlConnection = null;
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
                    deleted = responseJSON.optBoolean("operationStatus");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return deleted;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mDeleteTask = null;

            if (success) {

                Intent homeIntent = new Intent(getApplicationContext(),MoviesActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                finish();
                Toast.makeText(getBaseContext(), "The movie was successfully deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(), "Couldn't delete movie.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mDeleteTask = null;
        }
    }

    public class RentMovieTask extends AsyncTask<String, Void, Boolean> {
        String error = "";
        @Override
        protected Boolean doInBackground(String... params) {

            String RENT_MOVIE_WS = getString(R.string.rent_movie_ws);
            Boolean rented = false;

            try{
                SharedPreferences pref = getSharedPreferences("LoginPrefsFile",MODE_PRIVATE);
                int userId = pref.getInt("userid", -1);
                Uri builtUri = Uri.parse(RENT_WS).buildUpon()
                        .appendEncodedPath(RENT_MOVIE_WS)
                        .appendQueryParameter("movieID", params[0])
                        .appendQueryParameter("userID", String.valueOf(userId))
                        .build();
                URL url;
                HttpURLConnection urlConnection = null;
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
                    rented = responseJSON.optBoolean("rented");
                    mRentID = responseJSON.optInt("rentID");
                    if(!rented)
                    {
                        error = responseJSON.optString("error");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    error = e.getLocalizedMessage();
                } catch (JSONException e) {
                    error = e.getLocalizedMessage();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                error = e.getLocalizedMessage();
            }
            return rented;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mRentTask = null;
            mReturnRentError=false;
            if (success) {
                Toast.makeText(getBaseContext(), "You have rented the movie successfully. Enjoy!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(), "Couldn't rent movie." + error, Toast.LENGTH_SHORT).show();
                mReturnRentError=true;
                mRentMovieSwitch.setChecked(false);
            }
        }

        @Override
        protected void onCancelled() {
            mRentTask = null;
        }
    }


    public class ReturnMovieTask extends AsyncTask<Integer, Void, Boolean> {
        String error = "";
        @Override
        protected Boolean doInBackground(Integer... params) {


            String RETURN_MOVIE_WS= getString(R.string.return_movie_ws);
            Boolean returned = false;

            try{
                Uri builtUri = Uri.parse(RENT_WS).buildUpon()
                        .appendEncodedPath(RETURN_MOVIE_WS)
                        .appendQueryParameter("rentId", params[0].toString())
                        .build();
                URL url;
                HttpURLConnection urlConnection = null;
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
                    returned = responseJSON.optBoolean("returned");
                    if(!returned)
                    {
                        error = responseJSON.optString("error");
                    }else{
                        mRentID = -1;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    error = e.getLocalizedMessage();
                } catch (JSONException e) {
                    error = e.getLocalizedMessage();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                error = e.getLocalizedMessage();
            }
            return returned;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mReturnTask = null;

            if (success) {
                Toast.makeText(getBaseContext(), "You have returned the movie successfully.", Toast.LENGTH_SHORT).show();
                mReturnRentError=false;
            } else {
                mReturnRentError=true;
                mRentMovieSwitch.setChecked(true);
                Toast.makeText(getBaseContext(), "Couldn't return movie." + error, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mReturnTask = null;
        }
    }

    public class IsRentedTask extends AsyncTask<Void, Void, Boolean> {
        String error = "";
        @Override
        protected Boolean doInBackground(Void... params) {

            String CHECK__USER_RENT_MOVIE_WS= getString(R.string.check_user_rent_ws);
            Boolean isRented = false;
            SharedPreferences pref = getSharedPreferences("LoginPrefsFile",MODE_PRIVATE);
            int userId = pref.getInt("userid", -1);
            try{
                Uri builtUri = Uri.parse(RENT_WS).buildUpon()
                        .appendEncodedPath(CHECK__USER_RENT_MOVIE_WS)
                        .appendQueryParameter("userID",String.valueOf(userId) )
                        .appendQueryParameter("movieID", movie.getId())
                        .build();
                URL url;
                HttpURLConnection urlConnection = null;
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
                    isRented = responseJSON.optBoolean("isRented");
                    mRentID = responseJSON.optInt("rentID");
                    if(!isRented)
                    {
                        error = responseJSON.optString("error");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    error = e.getLocalizedMessage();
                } catch (JSONException e) {
                    error = e.getLocalizedMessage();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                error = e.getLocalizedMessage();
            }
            return isRented;
        }

        @Override
        protected void onPostExecute(final Boolean isRented) {
//            Toast.makeText(MovieDetailActivity.this, "movie is rented: " + isRented.toString(), Toast.LENGTH_SHORT).show();
            if(isRented) {
                mReturnRentError = true; //skip the listener
                mRentMovieSwitch.setChecked(isRented);
            }
            mIsRentedTask = null;
        }

        @Override
        protected void onCancelled() {
            mIsRentedTask = null;
        }
    }

}
