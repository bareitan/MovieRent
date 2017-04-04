package com.example.bareitan.movierent;

import android.content.Context;
import android.content.Intent;
import android.graphics.Movie;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class AddMovieFromTMDBActivity extends AppCompatActivity {
    RecyclerView mMoviesRV;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    EditText queryET;
    ArrayList<MovieItem> movies;
    private ProgressBar progressBar;
    Button searchButton;
    FindTmdbTask findTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movies = new ArrayList<MovieItem>();


        setContentView(R.layout.activity_add_movie_from_tmdb);
        mMoviesRV = (RecyclerView) findViewById(R.id.moviesRV);
        queryET = (EditText) findViewById(R.id.query);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (findTask == null) {
                    findTask = new FindTmdbTask();
                    findTask.execute((Void) null);
                } else {
                    Toast.makeText(AddMovieFromTMDBActivity.this, "Find task is in progress", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mMoviesRV.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mMoviesRV.setLayoutManager(mLayoutManager);

        mAdapter = new tmdbSearchAdapter(movies, getBaseContext());
        mMoviesRV.setAdapter(mAdapter);

    }

    public class FindTmdbTask extends AsyncTask<Void, Void, Integer> {
        String downloadUri;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            String TMDB_API = getString(R.string.tmdb_search_api);
            String TMDB_API_KEY = getString(R.string.tmdb_key);
            String query = queryET.getText().toString();

            try {
                Uri builtUri = Uri.parse(TMDB_API).buildUpon()
                        .appendQueryParameter("api_key", TMDB_API_KEY)
                        .appendQueryParameter("query", query)
                        .build();

                URL url = null;
                try {
                    url = new URL(builtUri.toString());

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                downloadUri = url.toString();
                Log.e("URI", downloadUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Integer result = 0;
            HttpURLConnection urlConnection;
            try {
                URL url = new URL(downloadUri);
                urlConnection = (HttpURLConnection) url.openConnection();
                int statusCode = urlConnection.getResponseCode();

                // 200 represents HTTP OK
                if (statusCode == 200) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }
                    parseFindResult(response.toString());
                    result = 1; // Successful
                } else {
                    result = 0; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d("FETCH_RESULTS", e.getLocalizedMessage());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {
            progressBar.setVisibility(View.GONE);
            findTask = null;

            if (result == 1) {
                mAdapter = new tmdbSearchAdapter(movies, getBaseContext());
                mMoviesRV.setAdapter(mAdapter);
            } else {
                Toast.makeText(AddMovieFromTMDBActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
        }
        private void parseFindResult(String result) {
            try {
                JSONObject response = new JSONObject(result);
                JSONArray moviesJSONArray = response.optJSONArray("results");
                movies = new ArrayList<>();

                for (int i = 0; i < moviesJSONArray.length(); i++) {
                    JSONObject movie = moviesJSONArray.optJSONObject(i);
                    MovieItem item = new MovieItem();
                    item.setName(movie.optString("title"));
                    item.setTmdbID(movie.optString("id"));
                    item.setYear(movie.optString("release_date"));
                    item.setThumbnail(movie.optString("poster_path"));
                    movies.add(item);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
