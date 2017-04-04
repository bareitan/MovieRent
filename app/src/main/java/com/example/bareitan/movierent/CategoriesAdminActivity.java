package com.example.bareitan.movierent;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class CategoriesAdminActivity extends AppCompatActivity {
    ArrayList<Category> categories;
    FloatingActionButton addButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories_admin);
        RecyclerView categoriesRV = (RecyclerView) findViewById(R.id.rvCategories);
        categories = new ArrayList<Category>();
        categories.add(new Category(1,"Action"));
        categories.add(new Category(2,"Comedy"));
        categories.add(new Category(3,"Drama"));

        final CategoriesAdapter adapter = new CategoriesAdapter(this, categories);
        categoriesRV.setAdapter(adapter);
        categoriesRV.setLayoutManager(new LinearLayoutManager(this));

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

}
