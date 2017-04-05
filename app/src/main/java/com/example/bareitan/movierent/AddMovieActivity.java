package com.example.bareitan.movierent;


import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class AddMovieActivity extends AppCompatActivity {
    private static final String TAG = "AddMovieActivity";
    EditText mMovieName;
    EditText mMovieOverview;
    Spinner mMovieCategorySpinner;
    EditText mStock;
    Button mSubmit;
    Button mAddTmdbButton;
    AddMovieTask  mAddMovieTask;
    EditText mThumbUrl;
    ArrayAdapter spinnerArrayAdapter;
    ArrayList<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

        mMovieName = (EditText) findViewById(R.id.movie_name);
        mMovieOverview = (EditText) findViewById(R.id.movie_overview);
        mMovieCategorySpinner = (Spinner) findViewById(R.id.movie_category);
        new GetCategoriesTask().execute();
        mStock = (EditText) findViewById(R.id.stock);
        mThumbUrl = (EditText) findViewById(R.id.thumb_url);
        mSubmit = (Button) findViewById(R.id.submit);
        mAddTmdbButton = (Button) findViewById(R.id.add_from_tmdb_button);
        MovieItem item;
        Intent callerIntent = getIntent();
        if(callerIntent.hasExtra("fetched_movie")){
            item = callerIntent.getParcelableExtra("fetched_movie");
            mMovieName.setText(item.getName());
            mMovieOverview.setText(item.getOverview());
//            mMovieCategory.setText(item.getCategoryName());
            mThumbUrl.setText(item.getThumbnail());
        }

        mAddTmdbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),AddMovieFromTMDBActivity.class));
            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                MovieItem movie = new MovieItem();
                movie.setName(mMovieName.getText().toString());
                movie.setOverview(mMovieOverview.getText().toString());
                Category category = (Category) mMovieCategorySpinner.getSelectedItem();
                movie.setCategoryName(category.getName());
                movie.setCategoryID(category.getId());
                movie.setStock(Integer.parseInt(mStock.getText().toString()));
                movie.setThumbnail(mThumbUrl.getText().toString());
                addMovie(movie);
                Toast.makeText(AddMovieActivity.this, "The movie was added successfully.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(),MoviesActivity.class);
                startActivity(intent);
                finish();

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
                        .appendQueryParameter("categoryId", Integer.toString(mMovie.getCategoryID()))
                        .appendQueryParameter("thumbnail", mMovie.getThumbnail())
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

    public class GetCategoriesTask extends AsyncTask<Void, Void, Integer> {
        String downloadUri;
        @Override
        protected void onPreExecute() {

            String RENT_WS = getString(R.string.ws);
            String ALL_CATEGORIES_WS = getString(R.string.all_categories_ws);
            try {
                Uri builtUri = Uri.parse(RENT_WS).buildUpon()
                        .appendEncodedPath(ALL_CATEGORIES_WS)
                        .build();

                URL url = null;
                try {
                    url = new URL(builtUri.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                downloadUri = url.toString();
            } catch(Exception e){
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
                    parseResult(response.toString());
                    result = 1; // Successful
                } else {
                    result = 0; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {

            if (result == 1) {


                spinnerArrayAdapter = new ArrayAdapter(AddMovieActivity.this,android.R.layout.simple_spinner_item, categories);
                mMovieCategorySpinner.setAdapter(spinnerArrayAdapter);
            } else {
                Toast.makeText(getBaseContext(), "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            JSONArray categoriesJSONArray = response.optJSONArray("Categories");
            categories = new ArrayList<>();

            for (int i = 0; i < categoriesJSONArray.length(); i++) {
                JSONObject categoryJSON = categoriesJSONArray.optJSONObject(i);
                Category category = new Category();
                category.setId(categoryJSON.optInt("categoryID"));
                category.setName(categoryJSON.optString("categoryName"));
                categories.add(category);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}

