package com.example.nicat.travelgram.Model;

public class postuser {

    private String namesurname,country,username,gmail,profilepicture,userid;

    public postuser(String namesurname, String country, String username, String gmail, String profilepicture, String userid) {
        this.namesurname = namesurname;
        this.country = country;
        this.username = username;
        this.gmail = gmail;
        this.profilepicture = profilepicture;
        this.userid = userid;
    }

    public postuser(){

    }

    public String getNamesurname() {
        return namesurname;
    }

    public void setNamesurname(String namesurname) {
        this.namesurname = namesurname;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getProfilepicture() {
        return profilepicture;
    }

    public void setProfilepicture(String profilepicture) {
        this.profilepicture = profilepicture;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
