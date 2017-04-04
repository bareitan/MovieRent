package com.example.bareitan.movierent;

import android.content.Context;
import android.graphics.Movie;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bareitan on 04/04/2017.
 */

public class tmdbSearchAdapter extends RecyclerView.Adapter<tmdbSearchAdapter.ViewHolder> {
    private ArrayList <MovieItem> mMovies;



    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mMovieName;
        public String tmdbID;


        public ViewHolder(View v) {
            super(v);
            mMovieName = (TextView)v.findViewById(R.id.movie_name);
        }
    }
    public tmdbSearchAdapter(ArrayList <MovieItem> movieDataSet ) {
        mMovies = movieDataSet;
    }

    @Override
    public tmdbSearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View movieView = inflater.inflate(R.layout.tmdb_movie_item, parent, false);

        tmdbSearchAdapter.ViewHolder viewHolder = new tmdbSearchAdapter.ViewHolder(movieView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(tmdbSearchAdapter.ViewHolder holder, int position) {
        MovieItem movie = mMovies.get(position);

        holder.mMovieName.setText(movie.getName() + " (" + movie.getYear() + ")");
        holder.tmdbID = movie.getTmdbID();

    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }


}
