package com.example.bareitan.movierent;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MovieDetailActivity extends AppCompatActivity {
    TextView mMovieNameTV;
    TextView mMovieOverviewTV;
    TextView mMovieCategoryTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        mMovieNameTV = (TextView)findViewById(R.id.movie_name);
        mMovieOverviewTV = (TextView)findViewById(R.id.movie_overview);
        mMovieCategoryTV = (TextView)findViewById(R.id.movie_category);

        Intent caller = getIntent();
        if(caller.hasExtra("movieName"))
        {
            String movieName = caller.getStringExtra("movieName");
            mMovieNameTV.setText(movieName);
        }
        if(caller.hasExtra("movieOverview")){
            mMovieOverviewTV.setText(caller.getStringExtra("movieOverview"));
        }
        if(caller.hasExtra("movieCategory")){
            mMovieCategoryTV.setText(caller.getStringExtra("movieCategory"));
        }
    }
}
