package com.example.bareitan.movierent;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MovieDetailActivity extends AppCompatActivity {
    TextView mMovieNameTV;
    TextView mMovieOverviewTV;
    TextView mMovieCategoryTV;
    Switch mRentMovieSwitch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        mMovieNameTV = (TextView)findViewById(R.id.movie_name);
        mMovieOverviewTV = (TextView)findViewById(R.id.movie_overview);
        mMovieCategoryTV = (TextView)findViewById(R.id.movie_category);
        mRentMovieSwitch = (Switch) findViewById(R.id.rent_movie);

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

        mRentMovieSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Toast.makeText(MovieDetailActivity.this, "You have rented the movie successfully.", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MovieDetailActivity.this, "Thank you for returning the movie.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
