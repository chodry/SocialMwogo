package com.example.chodry.socialmwogo;

public class Comments {

    public String comments, date, time, username;

    public Comments(){

    }

    public Comments(String comments, String date, String time, String username) {
        this.comments = comments;
        this.date = date;
        this.time = time;
        this.username = username;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getDate() {
        return date;
    }

    public void setData(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
