package com.hocnv.mobile_0410.data.models;

import com.google.firebase.Timestamp;

import java.util.List;

@SuppressWarnings("unused")
public class Ticket {
    public String id;
    public String userId;
    public String movieId;
    public String showtimeId;
    public String theaterId;
    public List<String> seats;
    public double totalPrice;
    public String status;         // PENDING / CONFIRMED / CANCELLED
    public Timestamp bookingTime;
    public String fcmToken;

    public Ticket() {}

    public Ticket(String id, String userId, String movieId, String showtimeId,
                  String theaterId, List<String> seats, double totalPrice,
                  String status, Timestamp bookingTime, String fcmToken) {
        this.id = id;
        this.userId = userId;
        this.movieId = movieId;
        this.showtimeId = showtimeId;
        this.theaterId = theaterId;
        this.seats = seats;
        this.totalPrice = totalPrice;
        this.status = status;
        this.bookingTime = bookingTime;
        this.fcmToken = fcmToken;
    }
}

