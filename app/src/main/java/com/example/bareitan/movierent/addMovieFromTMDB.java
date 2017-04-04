package com.example.bareitan.movierent;

import android.graphics.Movie;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class addMovieFromTMDB extends AppCompatActivity {
    RecyclerView mMoviesRV;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList<MovieItem> movies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movies = new ArrayList<MovieItem>();
        movies.add(new MovieItem());
        movies.add(new MovieItem());
        movies.get(0).setName("Fantasia");
        movies.get(0).setYear(2018);

        movies.get(1).setName("Furious");
        movies.get(1).setYear(2014);

        setContentView(R.layout.activity_add_movie_from_tmdb);
        mMoviesRV = (RecyclerView) findViewById(R.id.moviesRV);

        mMoviesRV.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mMoviesRV.setLayoutManager(mLayoutManager);

        mAdapter = new tmdbSearchAdapter(movies);
        mMoviesRV.setAdapter(mAdapter);

    }
}
