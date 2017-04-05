package com.example.bareitan.movierent;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ExpandedMenuView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.util.List;

public class MoviesActivity extends AppCompatActivity implements MoviesAdapter.ListItemClickListener{
    private static final String TAG = "MoviesActivity";
    private List<MovieItem> movieItemList;
    private RecyclerView mRecyclerView;
    private MoviesAdapter adapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        swipeRefreshLayout =(SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new DownloadTask().execute();
            }
        });
        new DownloadTask().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.categories_admin:
                Intent manageCategoriesIntent = new Intent(this, CategoriesAdminActivity.class);
                startActivity(manageCategoriesIntent);
                return true;
            case R.id.add_movie:
                Intent addMovieIntent = new Intent(this, AddMovieActivity.class);
                startActivity(addMovieIntent);
                return true;
            case R.id.sign_out:
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                loginIntent.putExtra("sign_out", true);
                Toast.makeText(this, "You have been signed out successfully", Toast.LENGTH_SHORT).show();
                startActivity(loginIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.admin_menu, menu);
        inflater.inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Context context = MoviesActivity.this;
        Class detailActivity = MovieDetailActivity.class;
        Intent intent = new Intent(context,detailActivity);
        intent.putExtra("movie", movieItemList.get(clickedItemIndex));
        startActivity(intent);
    }

    public class DownloadTask extends AsyncTask<Void, Void, Integer>{
        String downloadUri;
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);

            String RENT_WS = getString(R.string.ws);
            String MOVIES_WS = getString(R.string.all_movies_ws);
            try {
                Uri builtUri = Uri.parse(RENT_WS).buildUpon()
                        .appendEncodedPath(MOVIES_WS)
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
            progressBar.setVisibility(View.GONE);

            if (result == 1) {
                adapter = new MoviesAdapter(MoviesActivity.this, movieItemList,MoviesActivity.this);
                mRecyclerView.setAdapter(adapter);
            } else {
                Toast.makeText(MoviesActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            JSONArray movies = response.optJSONArray("Movies");
            movieItemList = new ArrayList<>();

            for (int i = 0; i < movies.length(); i++) {
                JSONObject movie = movies.optJSONObject(i);
                MovieItem item = new MovieItem();
                item.setId(movie.optString("movieID"));
                item.setName(movie.optString("movieName"));
                item.setOverview(movie.optString("overview"));
                item.setCategoryName(movie.optString("categoryName"));
                item.setThumbnail(movie.optString("thumbnail"));
                item.setStock(movie.optInt("stock"));
                movieItemList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}