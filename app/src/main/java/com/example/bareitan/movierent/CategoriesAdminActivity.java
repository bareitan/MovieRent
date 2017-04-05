package com.example.bareitan.movierent;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class CategoriesAdminActivity extends AppCompatActivity {
    private static final String TAG = "CategoriesAdminActivity";
    ArrayList<Category> categories;
    FloatingActionButton addButton;
    CategoriesAdapter adapter;
    RecyclerView categoriesRV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories_admin);
        categoriesRV = (RecyclerView) findViewById(R.id.rvCategories);


        categoriesRV.setLayoutManager(new LinearLayoutManager(this));
        new GetCategoriesTask().execute();

        addButton = (FloatingActionButton) findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CategoriesAdminActivity.this);
                LayoutInflater inflater = LayoutInflater.from(CategoriesAdminActivity.this);
                final View dialogView = inflater.inflate(R.layout.dialog_cateogry, null);
                dialogBuilder.setView(dialogView);

                final EditText edt = (EditText) dialogView.findViewById(R.id.category_name);

                dialogBuilder.setMessage("Enter a new category name");
                dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(getBaseContext(), "New Category name: " + edt.getText().toString(), Toast.LENGTH_SHORT).show();
                        categories.add(new Category(0,edt.getText().toString()));
                        adapter.notifyDataSetChanged();
                    }
                });
                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                AlertDialog b = dialogBuilder.create();
                b.show();
            }
        });



    }
    public class GetCategoriesTask extends AsyncTask<Void, Void, Integer> {
        String downloadUri;
        @Override
        protected void onPreExecute() {

            String RENT_WS = getString(R.string.ws);
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
                adapter = new CategoriesAdapter(CategoriesAdminActivity.this, categories);
                categoriesRV.setAdapter(adapter);
            } else {
                Toast.makeText(getBaseContext(), "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
        }
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
}
