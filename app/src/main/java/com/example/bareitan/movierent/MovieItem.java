package com.example.bareitan.movierent;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by bareitan on 31/03/2017.
 */

class MovieItem implements Parcelable{
    private String name;
    private String overview;
    private String categoryName;
    private String tmdbID;
    private String year;



    private String thumbnail;
    public MovieItem(){

    }


    protected MovieItem(Parcel in) {
        name = in.readString();
        overview = in.readString();
        categoryName = in.readString();
        tmdbID = in.readString();
        year = in.readString();
        thumbnail = in.readString();
        stock = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(overview);
        dest.writeString(categoryName);
        dest.writeString(tmdbID);
        dest.writeString(year);
        dest.writeString(thumbnail);
        dest.writeInt(stock);
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

}
