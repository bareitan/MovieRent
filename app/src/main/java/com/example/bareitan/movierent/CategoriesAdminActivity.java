package com.example.bareitan.movierent;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;

public class CategoriesAdminActivity extends AppCompatActivity {
    ArrayList<Category> categories;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories_admin);
        RecyclerView categoriesRV = (RecyclerView) findViewById(R.id.rvCategories);
        categories = new ArrayList<Category>();
        categories.add(new Category(1,"Action"));
        categories.add(new Category(2,"Comedy"));
        categories.add(new Category(3,"Drama"));

        CategoriesAdapter adapter = new CategoriesAdapter(this, categories);
        categoriesRV.setAdapter(adapter);
        categoriesRV.setLayoutManager(new LinearLayoutManager(this));

    }

}
