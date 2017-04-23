package com.example.bareitan.movierent;

import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by bareitan on 01/04/2017.
 */

public class CategoriesAdapter extends
        RecyclerView.Adapter<CategoriesAdapter.ViewHolder>
{
    UpdateCategoryTask mUpdateCategoryTask;
    String RENT_WS;
    private List<Category> mCategories;
    private Context mContext;

    public CategoriesAdapter(Context context, List<Category> categories) {
        mCategories = categories;
        mContext = context;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        RENT_WS = sharedPref.getString("ws_uri", "");
    }
    public Context getContext() {
        return mContext;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View categoryView = inflater.inflate(R.layout.item_category,parent,false);

        ViewHolder viewHolder = new ViewHolder(categoryView);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Category category = mCategories.get(position);

        final TextView categroyNameTV = holder.categoryNameTV;
        categroyNameTV.setText(category.getName());
        holder.categoryID = category.getId();

        Button deleteButton = holder.deleteCategoryButton;
        Button editButton = holder.editCategoryButton;
        final int id = mCategories.get(position).getId();
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DeleteCategoryTask(id).execute();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
                LayoutInflater inflater = LayoutInflater.from(mContext);
                final View dialogView = inflater.inflate(R.layout.dialog_cateogry, null);
                dialogBuilder.setView(dialogView);

                final EditText edt = (EditText) dialogView.findViewById(R.id.category_name);
                edt.setText(mCategories.get(position).getName());
                final int id = mCategories.get(position).getId();
                dialogBuilder.setMessage("Enter a new category name");
                dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new UpdateCategoryTask(id, edt.getText().toString()).execute();

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

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {

        public TextView categoryNameTV;
        public Button deleteCategoryButton;
        public Button editCategoryButton;
        public int categoryID;

        public ViewHolder(View itemView) {
            super(itemView);

            categoryNameTV = (TextView) itemView.findViewById(R.id.category_name);
            deleteCategoryButton = (Button) itemView.findViewById(R.id.delete_button);
            editCategoryButton = (Button) itemView.findViewById(R.id.edit_button);
        }

    }

    public class UpdateCategoryTask extends AsyncTask<Void, Void, Integer> {
        String downloadUri;
        String newCategoryName;
        int categoryID;

        public UpdateCategoryTask(int categoryID, String categoryName) {
            this.newCategoryName = categoryName;
            this.categoryID = categoryID;
        }
        @Override
        protected void onPreExecute() {

            String UPDATE_CATEGORIES_WS = mContext.getString(R.string.update_categories_ws);
            try {
                Uri builtUri = Uri.parse(RENT_WS).buildUpon()
                        .appendEncodedPath(UPDATE_CATEGORIES_WS)
                        .appendQueryParameter("newCategoryName",newCategoryName)
                        .appendQueryParameter("categoryID",String.valueOf(categoryID))
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
                    JSONObject resultJson = new JSONObject(response.toString());
                    if(resultJson.optBoolean("updated",false))
                        result=1;
                } else {
                    result = 0; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d("UPDATE_CATEGORY", e.getLocalizedMessage());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {

            if (result == 1) {
                CategoriesAdminActivity activity = (CategoriesAdminActivity) mContext;
                activity.updateCategories();

                Toast.makeText(mContext, "New Category name: " + newCategoryName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Failed to update category!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class DeleteCategoryTask extends AsyncTask<Void, Void, Integer> {
        String downloadUri;

        int categoryID;

        public DeleteCategoryTask(int categoryID) {

            this.categoryID = categoryID;
        }
        @Override
        protected void onPreExecute() {

            String DELETE_CATEGORIES_WS = mContext.getString(R.string.delete_categories_ws);
            try {
                Uri builtUri = Uri.parse(RENT_WS).buildUpon()
                        .appendEncodedPath(DELETE_CATEGORIES_WS)
                        .appendQueryParameter("categoryID",String.valueOf(categoryID))
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
                    JSONObject resultJson = new JSONObject(response.toString());
                    if(resultJson.optBoolean("deleted",false))
                        result=1;
                } else {
                    result = 0; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d("DELETE_CATEGORY", e.getLocalizedMessage());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {

            if (result == 1) {
                CategoriesAdminActivity activity = (CategoriesAdminActivity) mContext;
                activity.updateCategories();

                Toast.makeText(mContext, "Category deleted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Failed to delete category!", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
