package com.hocnv.mobile_0410.data.models;

import com.google.firebase.Timestamp;

@SuppressWarnings("unused")
public class AppUser {
    public String uid;
    public String email;
    public String displayName;
    public String photoUrl;
    public Timestamp createdAt;
    public String fcmToken;

    public AppUser() {}

    public AppUser(String uid, String email, String displayName, String photoUrl, Timestamp createdAt) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.createdAt = createdAt;
    }
}

