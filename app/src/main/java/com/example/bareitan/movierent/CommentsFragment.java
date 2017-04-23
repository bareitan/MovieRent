package com.example.bareitan.movierent;

import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
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

import static android.view.View.GONE;


public class CommentsFragment extends Fragment
        implements AdapterCallback {
    MovieItem currentMovie;
    List<Comment> commentsList;
    CommentsAdapter adapter;
    RecyclerView mCommentsRV;
    String movieId;
    EditText commentET;
    String userId;
    RatingBar ratingBar;
    Button submit;
    AddCommentTask addCommentTask = null;
    DeleteCommentTask deleteCommentTask = null;
    String RENT_WS;

    @Override
    public void onMethodCallback(int commentId) {
        Log.d("CALLBACK", "test");
        if (deleteCommentTask == null) {
            deleteCommentTask = new DeleteCommentTask(commentId);
            deleteCommentTask.execute();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comments, container, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        RENT_WS = sharedPref.getString("ws_uri", "");

        mCommentsRV = (RecyclerView) view.findViewById(R.id.comments_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mCommentsRV.setLayoutManager(new GridLayoutManager(getContext(), 1));

        commentET = (EditText) view.findViewById(R.id.comment_text);
        ratingBar = (RatingBar) view.findViewById(R.id.rating_bar);
        submit = (Button) view.findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentET.setError(null);
                if (commentET.getText().toString().isEmpty()) {
                    commentET.setError("Comment can not be empty.");
                    commentET.requestFocus();
                } else if (addCommentTask == null) {
                    addCommentTask = new AddCommentTask();
                    addCommentTask.execute();
                }
            }
        });

        Intent caller = getActivity().getIntent();
        MovieItem movie = caller.getParcelableExtra("movie");
        movieId = movie.getId();
        new GetCommentsTask().execute();
        return view;
    }

    int getAverageRating() {
        int sum = 0;
        for (Comment comment : commentsList) {
            sum += comment.getRating();
        }
        if (commentsList.size() > 0)
            return sum / commentsList.size();
        else
            return 0;
    }

    Boolean userCommented() {
        Boolean commented = false;
        String PREFS_LOGIN = "LoginPrefsFile";
        String PREF_USER_ID = "userid";

        SharedPreferences pref = getContext().getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
        int userId = pref.getInt(PREF_USER_ID, -1);
        for (Comment comment : commentsList) {
            if (comment.getUserId() == userId)
                commented = true;
        }
        return commented;
    }

    public class GetCommentsTask extends AsyncTask<Void, Void, Integer> {
        String getCommentsUri;
        String error;

        @Override
        protected void onPreExecute() {

            String COMMENTS_WS = getString(R.string.comments_ws);
            try {
                Uri builtUri = Uri.parse(RENT_WS).buildUpon()
                        .appendEncodedPath(COMMENTS_WS)
                        .appendPath(movieId)
                        .build();
                Log.d("GET_COMMENTS_URI: ", builtUri.toString());
                URL url = null;
                try {
                    url = new URL(builtUri.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                getCommentsUri = url.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Integer result = 0;
            HttpURLConnection urlConnection;
            try {
                URL url = new URL(getCommentsUri);
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
                Log.d("GET_COMMENTS", e.getLocalizedMessage());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {

            if (result == 1) {
                adapter = new CommentsAdapter(getContext(), commentsList, CommentsFragment.this);
                mCommentsRV.setAdapter(adapter);

                if (userCommented()) {
                    commentET.setVisibility(GONE);
                    submit.setVisibility(GONE);
                    ratingBar.setIsIndicator(true);
                    ratingBar.setRating(getAverageRating());
                }
            } else {
                if (error != "")
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getContext(), "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }

        }

        private void parseResult(String result) {
            try {
                JSONObject response = new JSONObject(result);
                JSONArray comments = response.optJSONArray("Comments");
                error = response.optString("error");
                commentsList = new ArrayList<>();

                for (int i = 0; i < comments.length(); i++) {
                    JSONObject comment = comments.optJSONObject(i);
                    Comment item = new Comment();
                    item.setText(comment.optString("text"));
                    item.setUser(comment.optString("userName"));
                    item.setRating(comment.optInt("rating"));
                    item.setUserId(comment.optInt("userId"));
                    item.setCommentId(comment.optInt("commentId"));
                    commentsList.add(item);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class AddCommentTask extends AsyncTask<Void, Void, Boolean> {
        String commentsUri;
        String error;
        Boolean success = false;

        @Override
        protected void onPreExecute() {

            String COMMENTS_WS = getString(R.string.comments_ws);
            try {
                String PREFS_LOGIN = "LoginPrefsFile";
                String PREF_USER_ID = "userid";

                SharedPreferences pref = getContext().getSharedPreferences(PREFS_LOGIN, Context.MODE_PRIVATE);
                userId = String.valueOf(pref.getInt(PREF_USER_ID, -1));
                Uri builtUri = Uri.parse(RENT_WS).buildUpon()
                        .appendEncodedPath(COMMENTS_WS)
                        .appendPath(movieId)
                        .appendPath(userId)
                        .appendPath(commentET.getText().toString())
                        .appendPath(String.valueOf(ratingBar.getRating()))
                        .build();
                Log.d("COMMENTS_URI: ", builtUri.toString());
                URL url = null;
                try {
                    url = new URL(builtUri.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                commentsUri = url.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            HttpURLConnection urlConnection;
            try {
                URL url = new URL(commentsUri);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
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
                    success = true; // Successful
                } else {
                    success = false; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d("GET_COMMENTS", e.getLocalizedMessage());
            }
            return success; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if (success) {
                Toast.makeText(getContext(), "Comment was added successfully.", Toast.LENGTH_SHORT).show();
                new GetCommentsTask().execute();
            } else {
                if (error != "")
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getContext(), "Failed to add comment.", Toast.LENGTH_SHORT).show();
            }
            addCommentTask = null;
        }

        private void parseResult(String result) {
            try {
                JSONObject response = new JSONObject(result);
                success = response.optBoolean("added");
                error = response.optString("error");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class DeleteCommentTask extends AsyncTask<Void, Void, Boolean> {
        String commentsUri;
        String error;
        Boolean success = false;
        int commentId;

        public DeleteCommentTask(int commentId) {
            this.commentId = commentId;
        }

        @Override
        protected void onPreExecute() {
            Log.d("DELETE_COMMENTS", "ENTERED");
            String COMMENTS_WS = getString(R.string.comments_ws);
            try {
                Uri builtUri = Uri.parse(RENT_WS).buildUpon()
                        .appendEncodedPath(COMMENTS_WS)
                        .appendPath(String.valueOf(commentId))
                        .build();
                Log.d("COMMENTS_URI: ", builtUri.toString());
                URL url = null;
                try {
                    url = new URL(builtUri.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                commentsUri = url.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            HttpURLConnection urlConnection;
            try {
                URL url = new URL(commentsUri);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("DELETE");
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
                    success = true; // Successful
                } else {
                    success = false; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d("GET_COMMENTS", e.getLocalizedMessage());
            }
            return success; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if (success) {
                Toast.makeText(getContext(), "Comment was deleted successfully.", Toast.LENGTH_SHORT).show();
                commentET.setVisibility(View.VISIBLE);
                submit.setVisibility(View.VISIBLE);
                ratingBar.setIsIndicator(false);
                ratingBar.setRating(2);
                new GetCommentsTask().execute();
            } else {
                if (error != "")
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getContext(), "Failed to delete comment.", Toast.LENGTH_SHORT).show();
            }
            deleteCommentTask = null;
        }

        private void parseResult(String result) {
            try {
                JSONObject response = new JSONObject(result);
                success = response.optBoolean("deleted");
                error = response.optString("error");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}