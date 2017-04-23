package com.example.bareitan.movierent;
//Activity for adding movies

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
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
    ArrayAdapter mSpinnerArrayAdapter;
    ArrayList<Category> categories;
    ToggleButton mToggleCategory;
    EditText mNewCategory;
    GetCategoriesTask getCategoriesTask;
    MovieItem item;
    String mode="NEW";
    private String RENT_WS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

        mMovieName = (EditText) findViewById(R.id.movie_name);
        mMovieOverview = (EditText) findViewById(R.id.movie_overview);
        mMovieCategorySpinner = (Spinner) findViewById(R.id.movie_category);


        mStock = (EditText) findViewById(R.id.stock);
        mThumbUrl = (EditText) findViewById(R.id.thumb_url);
        mSubmit = (Button) findViewById(R.id.submit);
        mAddTmdbButton = (Button) findViewById(R.id.add_from_tmdb_button);
        mNewCategory = (EditText) findViewById(R.id.new_movie_category);
        mToggleCategory = (ToggleButton) findViewById(R.id.toggleNewCategory);
        mToggleCategory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    mMovieCategorySpinner.setVisibility(View.GONE);
                    mNewCategory.setVisibility(View.VISIBLE);
                }else {
                    mMovieCategorySpinner.setVisibility(View.VISIBLE);
                    mNewCategory.setVisibility(View.GONE);
                }
            }
        });

        Intent callerIntent = getIntent();
        if(callerIntent.hasExtra("MODE")){
            mode = callerIntent.getStringExtra("MODE");
            if(mode.equals("NEW_TMDB")){
                mAddTmdbButton.setVisibility(View.GONE);
            }else if(mode.equals("EDIT")){
                mAddTmdbButton.setVisibility(View.GONE);
                mSubmit.setText(R.string.update);
            }
        }
        if(callerIntent.hasExtra("fetched_movie")){
            item = callerIntent.getParcelableExtra("fetched_movie");
            mMovieName.setText(item.getName());
            mMovieOverview.setText(item.getOverview());
            mThumbUrl.setText(item.getThumbnail());
            mStock.setText(String.valueOf(item.getStock()));
        }

        getCategoriesTask = new GetCategoriesTask();
        getCategoriesTask.execute();

        mAddTmdbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),AddMovieFromTMDBActivity.class));
            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(!validateInput(mode)) {
                    MovieItem movie = new MovieItem();
                    if (mode.equals("EDIT")) {
                        movie.setId(item.getId());
                    }
                    movie.setName(mMovieName.getText().toString());
                    movie.setOverview(mMovieOverview.getText().toString());

                    if (mToggleCategory.isChecked()) {
                        movie.setCategoryName(mNewCategory.getText().toString());
                    } else {
                        Category category = (Category) mMovieCategorySpinner.getSelectedItem();
                        movie.setCategoryName(category.getName());
                    }

                    movie.setStock(Integer.parseInt(mStock.getText().toString()));
                    movie.setThumbnail(mThumbUrl.getText().toString());
                    addMovie(movie);
                }

            }
        });

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        RENT_WS = sharedPref.getString("ws_uri", "");


    }

    private boolean validateInput(String mode) {
        Boolean errors = false;
        mNewCategory.setError(null);
        mThumbUrl.setError(null);
        mMovieOverview.setError(null);
        mMovieName.setError(null);
        mStock.setError(null);

        int stock = Integer.parseInt(mStock.getText().toString());
        View focusView = null;

            if(stock < 1)
            {
                errors=true;
                mStock.setError("Stock must be greater than 0.");
                focusView = mStock;
            }

        if(mToggleCategory.isChecked() && mNewCategory.getText().toString().isEmpty())
        {
            errors = true;
            mNewCategory.setError("Category is mandatory.");
            focusView = mNewCategory;
        }
        if(mThumbUrl.getText().toString().isEmpty())
        {
            errors = true;
            mThumbUrl.setError("Thumb url is mandatory");
            focusView = mThumbUrl;
        }

        if(mMovieOverview.getText().toString().isEmpty())
        {
            errors = true;
            mMovieOverview.setError("Overview is mandatory.");
            focusView = mMovieOverview;
        }
        if (mMovieName.getText().toString().isEmpty())
        {
            errors = true;
            mMovieName.setError("Movie name is mandatory.");
            focusView = mMovieName;
        }
        if(focusView!=null) {
            focusView.requestFocus();
        }
        return errors;
    }

    private void addMovie(MovieItem movie) {
        if(mAddMovieTask != null){
            return;
        }
        mAddMovieTask = new AddMovieTask(movie);
        mAddMovieTask.execute((Void)null);
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

    public class AddMovieTask extends AsyncTask<Void, Void, Boolean> {
        public final MovieItem mMovie;
        String error;

        public AddMovieTask(MovieItem mMovie) {
            this.mMovie = mMovie;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success)
            {
                Toast.makeText(AddMovieActivity.this, "Done.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), MoviesActivity.class);
                startActivity(intent);
                finish();
            }
            else
            {
                Toast.makeText(AddMovieActivity.this, error, Toast.LENGTH_SHORT).show();
            }
            mAddMovieTask = null;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            String ADD_MOVIE_WS = getString(R.string.add_movie_ws);
            String UPDATE_MOVIE_WS = getString(R.string.update_movie_ws);
            Boolean successfully = false;

            Uri builtUri;

            try {
                if(mode.equals("EDIT"))
                {
                    builtUri = Uri.parse(RENT_WS).buildUpon()
                            .appendEncodedPath(UPDATE_MOVIE_WS)
                            .appendQueryParameter("movieName", mMovie.getName())
                            .appendQueryParameter("overview", mMovie.getOverview())
                            .appendQueryParameter("stock", Integer.toString(mMovie.getStock()))
                            .appendQueryParameter("categoryName", mMovie.getCategoryName())
                            .appendQueryParameter("thumbnail", mMovie.getThumbnail())
                            .appendQueryParameter("movieId", mMovie.getId())
                            .build();
                }
                else
                {
                    builtUri = Uri.parse(RENT_WS).buildUpon()
                            .appendEncodedPath(ADD_MOVIE_WS)
                            .appendQueryParameter("movieName", mMovie.getName())
                            .appendQueryParameter("overview", mMovie.getOverview())
                            .appendQueryParameter("stock", Integer.toString(mMovie.getStock()))
                            .appendQueryParameter("categoryName", mMovie.getCategoryName())
                            .appendQueryParameter("thumbnail", mMovie.getThumbnail())
                            .build();
                }

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
                    error = responseJSON.optString("error");
                    if(error!="")
                    {
                        successfully=false;
                    }
                    successfully = responseJSON.optBoolean("operationStatus");


                } catch (Exception e) {
                    error = e.getLocalizedMessage();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                error = e.getLocalizedMessage();
            }
            return successfully;
        }
    }

    public class GetCategoriesTask extends AsyncTask<Void, Void, Integer> {
        String downloadUri;
        @Override
        protected void onPreExecute() {

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
                mSpinnerArrayAdapter = new ArrayAdapter(AddMovieActivity.this,R.layout.support_simple_spinner_dropdown_item, categories);
                mMovieCategorySpinner.setAdapter(mSpinnerArrayAdapter);
                Log.d("ON_POST_EXECUTE","true");
                if(item!=null){
                    int spinnerPosition = mSpinnerArrayAdapter.getPosition(new Category(item.getCategoryID(),item.getCategoryName()));
                    if(spinnerPosition==-1)
                    {
                        mToggleCategory.setChecked(true);
                        mNewCategory.setText(item.getCategoryName());
                    }
                    else{
                        mMovieCategorySpinner.setSelection(spinnerPosition);
                    }

                }
            } else {
                Toast.makeText(getBaseContext(), "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
            getCategoriesTask = null;
        }
    }


}
