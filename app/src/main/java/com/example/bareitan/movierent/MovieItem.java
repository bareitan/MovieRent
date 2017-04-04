package com.example.bareitan.movierent;

import android.content.Intent;

/**
 * Created by bareitan on 31/03/2017.
 */

private class MovieItem {
    private String name;
    private String overview;
    private String categoryName;
    private String tmdbID;
    private int year;


    private int categoryID;
    private String thumbnail;


    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getTmdbID() {
        return tmdbID;
    }

    public void setTmdbID(String tmdbID) {
        this.tmdbID = tmdbID;
    }
    public int getStock() { return stock; }

    private int stock;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getCategoryName() {return categoryName;}

    public void setCategoryName(String categoryName) {this.categoryName = categoryName;}

    public String getOverview() {return overview;}

    public void setOverview(String overview) {this.overview = overview;}

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }
}
