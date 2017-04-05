package com.example.bareitan.movierent;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditMovieActivity extends AppCompatActivity {
    MovieItem movie;
    EditText mMovieName;
    EditText mMovieOverview;
    Spinner mMovieCategory;
    EditText mStock;
    Button mSubmit;
    Button mEditButton;
    EditMovieTask  mEditMovieTask;
    EditText mThumbUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_movie);

        mMovieName = (EditText) findViewById(R.id.movie_name);
        mMovieOverview = (EditText) findViewById(R.id.movie_overview);
        mMovieCategory = (Spinner) findViewById(R.id.movie_category);
        mStock = (EditText) findViewById(R.id.stock);
        mThumbUrl = (EditText) findViewById(R.id.thumb_url);
        mSubmit = (Button) findViewById(R.id.submit);

        Intent intent = getIntent();
        movie = intent.getParcelableExtra("movie");

        mMovieName.setText(movie.getName());
        mMovieOverview.setText(movie.getOverview());
        mStock.setText(String.valueOf(movie.getStock()));

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                movie.setName(mMovieName.getText().toString());
                movie.setOverview(mMovieOverview.getText().toString());
                movie.setStock(Integer.parseInt(mStock.getText().toString()));
                editMovie(movie);
            }
        });


    }


    private void editMovie(MovieItem movie) {
        if(mEditMovieTask != null){
            return;
        }
        mEditMovieTask = new EditMovieActivity.EditMovieTask(movie);
        mEditMovieTask.execute((Void)null);
    }

    public class EditMovieTask extends AsyncTask<Void, Void, Boolean> {

        public final MovieItem mMovie;

        public EditMovieTask(MovieItem mMovie) {
            this.mMovie = mMovie;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            String RENT_WS = getString(R.string.ws);
            String UPDATE_MOVIE_WS = getString(R.string.update_movie_ws);
            Boolean updated = false;
            String error;
            try {
                Uri builtUri = Uri.parse(RENT_WS).buildUpon()
                        .appendEncodedPath(UPDATE_MOVIE_WS)
                        .appendQueryParameter("movieName", mMovie.getName())
                        .appendQueryParameter("overview", mMovie.getOverview())
                        .appendQueryParameter("stock", Integer.toString(mMovie.getStock()))
                        .appendQueryParameter("categoryId", "1")
                        .appendQueryParameter("thumbnail", mMovie.getThumbnail())
                        .appendQueryParameter("movieId", mMovie.getId())
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
                    updated = responseJSON.optBoolean("operationStatus");
//                    error = responseJSON.optString("error");
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
            return updated;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mEditMovieTask = null;

            if (success) {

                Intent homeIntent = new Intent(getApplicationContext(),MoviesActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                finish();
                Toast.makeText(getBaseContext(), "The movie was successfully updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(), "Coudln't update movie.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
