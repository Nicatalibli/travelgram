package com.example.nicat.travelgram.Adapter;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Allitem implements ClusterItem {

    private LatLng mPosition;
    private String mTitle;
    private String mSnippet;
    private int iconPicture;

    public Allitem(double lat, double lng , String title) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
    }

    public Allitem(double lat, double lng, String mTitle, String mSnippet){
        this.mPosition = new LatLng(lat, lng);
        this.mTitle = mTitle;

        this.mSnippet = mSnippet;
    }




    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }


}
