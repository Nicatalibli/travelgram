package com.example.nicat.travelgram;

public class getuserphotodata {
    public double latitude;
    public double longitude;
    public String photodate;
    public String photourl;

    public getuserphotodata(){

    }

    public getuserphotodata(double latitude, double longitude,String photodate,String photourl){
        this.latitude = latitude;
        this.longitude = longitude;
        this.photodate = photodate;
        this.photourl = photourl;
    }

}
