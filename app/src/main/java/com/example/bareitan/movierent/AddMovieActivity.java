package com.example.bareitan.movierent;


import android.content.Intent;
import android.graphics.Movie;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AddMovieActivity extends AppCompatActivity {
    EditText mMovieName;
    EditText mMovieOverview;
    Spinner mMovieCategory;
    EditText mStock;
    Button mSubmit;
    Button mAddTmdbButton;
    AddMovieTask  mAddMovieTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

        mMovieName = (EditText) findViewById(R.id.movie_name);
        mMovieOverview = (EditText) findViewById(R.id.movie_overview);
        mMovieCategory = (Spinner) findViewById(R.id.movie_category);
        mStock = (EditText) findViewById(R.id.stock);
        mSubmit = (Button) findViewById(R.id.submit);
        mAddTmdbButton = (Button) findViewById(R.id.add_from_tmdb_button);

        mAddTmdbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),addMovieFromTMDB.class));
            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                MovieItem movie = new MovieItem();
                movie.setName(mMovieName.getText().toString());
                movie.setOverview(mMovieOverview.getText().toString());
                movie.setCategoryName((String) mMovieCategory.getSelectedItem());
                movie.setStock(Integer.parseInt(mStock.getText().toString()));
                addMovie(movie);

            }
        });



    }

    private void addMovie(MovieItem movie) {
        if(mAddMovieTask != null){
            return;
        }
        mAddMovieTask = new AddMovieTask(movie);
        mAddMovieTask.execute((Void)null);
    }

    public class AddMovieTask extends AsyncTask<Void, Void, Boolean> {

        public final MovieItem mMovie;

        public AddMovieTask(MovieItem mMovie) {
            this.mMovie = mMovie;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            String RENT_WS = getString(R.string.ws);
            String ADD_MOVIE_WS = getString(R.string.add_movie_ws);
            Boolean addedSuccessfully = false;
            String error;
            try {
                Uri builtUri = Uri.parse(RENT_WS).buildUpon()
                        .appendEncodedPath(ADD_MOVIE_WS)
                        .appendQueryParameter("movieName", mMovie.getName())
                        .appendQueryParameter("overview", mMovie.getOverview())
                        .appendQueryParameter("stock", Integer.toString(mMovie.getStock()))
                        .appendQueryParameter("categoryId", "1")
                        .appendQueryParameter("categoryId", Integer.toString(mMovie.getCategoryID()))
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
                    addedSuccessfully = responseJSON.optBoolean("addedSuccessfully");
                    error = responseJSON.optString("error");
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
            return addedSuccessfully;
        }
    }
}

