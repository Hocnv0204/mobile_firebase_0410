package com.hocnv.mobile_0410.data.models;

import com.google.firebase.Timestamp;

import java.util.Map;

@SuppressWarnings("unused")
public class Showtime {
    public String id;
    public String movieId;
    public String theaterId;
    public Timestamp startTime;
    public Timestamp endTime;
    public double price;
    public int availableSeats;
    public Map<String, Boolean> seatMap;

    public Showtime() {}

    public Showtime(String id, String movieId, String theaterId,
                    Timestamp startTime, Timestamp endTime, double price,
                    int availableSeats, Map<String, Boolean> seatMap) {
        this.id = id;
        this.movieId = movieId;
        this.theaterId = theaterId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.availableSeats = availableSeats;
        this.seatMap = seatMap;
    }
}

