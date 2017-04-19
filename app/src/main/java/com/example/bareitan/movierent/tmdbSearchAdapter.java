package com.example.bareitan.movierent;

import android.content.Context;
import android.content.Intent;
import android.graphics.Movie;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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

/**
 * Created by bareitan on 04/04/2017.
 */

public class tmdbSearchAdapter extends RecyclerView.Adapter<tmdbSearchAdapter.ViewHolder> {
    private ArrayList <MovieItem> mMovies;
    Context context;



    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mMovieName;
        public ImageView mThumbnail;
        public String tmdbID;
        SearchOneTmdbTask tmdbSearchTask;


        public ViewHolder(View v){
            super(v);
            mMovieName = (TextView)v.findViewById(R.id.title);
            mThumbnail = (ImageView) v.findViewById(R.id.thumbnail);
            v.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    tmdbSearchTask = new SearchOneTmdbTask();
                    tmdbSearchTask.execute(tmdbID);
                }
            });
        }
    }
    public tmdbSearchAdapter(ArrayList <MovieItem> movieDataSet, Context context ) {
        mMovies = movieDataSet;
        this.context = context;
    }

    @Override
    public tmdbSearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View movieView = inflater.inflate(R.layout.list_movie, parent, false);

        tmdbSearchAdapter.ViewHolder viewHolder = new tmdbSearchAdapter.ViewHolder(movieView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(tmdbSearchAdapter.ViewHolder holder, int position) {
        MovieItem movie = mMovies.get(position);

        holder.mMovieName.setText(movie.getName() + " (" + movie.getYear() + ")");
        holder.tmdbID = movie.getTmdbID();

        if(!TextUtils.isEmpty(movie.getThumbnail())) {
            Picasso.with(context).load(context.getString(R.string.poster_path) + movie.getThumbnail())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.mThumbnail);

        }


    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }


    public class SearchOneTmdbTask extends AsyncTask<String, Void, Integer> {
        String downloadUri;


        @Override
        protected Integer doInBackground(String... params) {

            String TMDB_MOVIE_API = context.getString(R.string.tmdb_movie_api);
            String TMDB_API_key = context.getString(R.string.tmdb_key);

            try {
                Uri builtUri = Uri.parse(TMDB_MOVIE_API).buildUpon()
                        .appendEncodedPath(params[0])
                        .appendQueryParameter("api_key", TMDB_API_key)
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

                    MovieItem resultMovie = parseSearchOneResult(response.toString());
                    Intent addMovieIntent = new Intent(context, AddMovieActivity.class);
                    addMovieIntent.putExtra("fetched_movie",resultMovie);
                    addMovieIntent.putExtra("MODE","NEW_TMDB");
                    context.startActivity(addMovieIntent);
                    result = 1;
                } else {
                    result = 0;
                }
            } catch (Exception e) {
                Log.d("FETCH_RESULTS", e.getLocalizedMessage());
                e.printStackTrace();
            }
            return result;
        }




        private MovieItem parseSearchOneResult(String result) {
            MovieItem item = null;
            try {
                JSONObject response = new JSONObject(result);
                JSONArray genres = response.optJSONArray("genres");
                JSONObject genre = genres.optJSONObject(0);

                String categoryName = genre.optString("name");

                item = new MovieItem();
                item.setName(response.optString("title"));
                item.setTmdbID(response.optString("id"));
                item.setYear(response.optString("release_date"));
                item.setThumbnail(context.getString(R.string.poster_path) + response.optString("poster_path"));
                item.setOverview(response.optString("overview"));
                item.setCategoryName(categoryName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return item;
        }
    }
}
