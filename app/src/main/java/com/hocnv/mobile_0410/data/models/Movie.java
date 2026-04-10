package com.hocnv.mobile_0410.data.models;

import com.google.firebase.Timestamp;

@SuppressWarnings("unused")
public class Movie {
    public String id;
    public String title;
    public String description;
    public String genre;
    public int duration;          // phút
    public float rating;
    public String posterUrl;
    public String trailerUrl;
    public Timestamp releaseDate;
    public boolean isNowShowing;

    public Movie() {}

    public Movie(String id, String title, String description, String genre,
                 int duration, float rating, String posterUrl, String trailerUrl,
                 Timestamp releaseDate, boolean isNowShowing) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.duration = duration;
        this.rating = rating;
        this.posterUrl = posterUrl;
        this.trailerUrl = trailerUrl;
        this.releaseDate = releaseDate;
        this.isNowShowing = isNowShowing;
    }
}
