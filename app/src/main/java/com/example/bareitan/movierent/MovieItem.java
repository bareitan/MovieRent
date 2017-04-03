package com.example.bareitan.movierent;

/**
 * Created by bareitan on 31/03/2017.
 */

public class MovieItem {
    private String name;
    private String overview;
    private String category;
    private String thumbnail;

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

    public String getCategory() {return category;}

    public void setCategory(String category) {this.category = category;}

    public String getOverview() {return overview;}

    public void setOverview(String overview) {this.overview = overview;}
}
