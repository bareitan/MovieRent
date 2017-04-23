package com.example.bareitan.movierent;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private static final String TAG = "HistoryActivity";
    String RENT_WS;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<RentItem> mRentItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        RENT_WS = sharedPref.getString("ws_uri", "");

        mRecyclerView = (RecyclerView) findViewById(R.id.rent_rv);

        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        new GetHistoryTask().execute();
    }

    private void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            JSONArray rents = response.optJSONArray("RentItems");
            mRentItemList = new ArrayList<>();

            for (int i = 0; i < rents.length(); i++) {
                JSONObject rent = rents.optJSONObject(i);
                RentItem item = new RentItem();
                item.setMovieName(rent.optString("movieName"));
                item.setMovieID(rent.optInt("movieID"));
                item.setRentDate(rent.optString("rentDate"));
                item.setReturnDate(rent.optString("returnDate"));

                mRentItemList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class GetHistoryTask extends AsyncTask<Void, Void, Integer> {
        String downloadUri;
        @Override
        protected void onPreExecute() {

            String HISTORY_WS = getString(R.string.history_ws);

            String PREFS_LOGIN = "LoginPrefsFile";
            String PREF_USER_ID = "userid";

            SharedPreferences pref = getSharedPreferences(PREFS_LOGIN,MODE_PRIVATE);
            int currentUser = pref.getInt(PREF_USER_ID, -1);

            try {
                Uri builtUri = Uri.parse(RENT_WS).buildUpon()
                        .appendEncodedPath(HISTORY_WS)
                        .appendPath(String.valueOf(currentUser))
                        .build();
                Log.d("HISTORY URI: ", builtUri.toString());
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
                mAdapter  = new HistoryAdapter(HistoryActivity.this,mRentItemList);
                mRecyclerView.setAdapter(mAdapter);
            } else {
                Toast.makeText(HistoryActivity.this, "Failed to fetch history data!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}


