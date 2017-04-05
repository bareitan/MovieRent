package com.example.bareitan.movierent;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by bareitan on 31/03/2017.
 */

class MovieItem implements Parcelable{


    private String id;
    private String name;
    private String overview;
    private String categoryName;
    private String tmdbID;
    private String year;
    private int stock;
    private int categoryID;

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    private String thumbnail;
    public MovieItem(){

    }


    protected MovieItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        overview = in.readString();
        categoryName = in.readString();
        tmdbID = in.readString();
        year = in.readString();
        stock = in.readInt();
        categoryID = in.readInt();
        thumbnail = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(overview);
        dest.writeString(categoryName);
        dest.writeString(tmdbID);
        dest.writeString(year);
        dest.writeInt(stock);
        dest.writeInt(categoryID);
        dest.writeString(thumbnail);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MovieItem> CREATOR = new Creator<MovieItem>() {
        @Override
        public MovieItem createFromParcel(Parcel in) {
            return new MovieItem(in);
        }

        @Override
        public MovieItem[] newArray(int size) {
            return new MovieItem[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getTmdbID() {
        return tmdbID;
    }

    public void setTmdbID(String tmdbID) {
        this.tmdbID = tmdbID;
    }
    public int getStock() { return stock; }



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

}
