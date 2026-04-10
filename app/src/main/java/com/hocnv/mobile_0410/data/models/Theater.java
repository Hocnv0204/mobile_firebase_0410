package com.hocnv.mobile_0410.data.models;

@SuppressWarnings("unused")
public class Theater {
    public String id;
    public String name;
    public String address;
    public String city;
    public int totalSeats;

    public Theater() {}

    public Theater(String id, String name, String address, String city, int totalSeats) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.totalSeats = totalSeats;
    }
}

