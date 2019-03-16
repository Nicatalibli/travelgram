package com.example.nicat.travelgram.Model;

public class user {

    private String namesurname,country,username,gmail;

    public user(String namesurname, String country, String username, String gmail) {
        this.namesurname = namesurname;
        this.country = country;
        this.username = username;
        this.gmail = gmail;
    }


    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getNamesurname() {
        return namesurname;
    }

    public void setNamesurname(String namesurname) {
        this.namesurname = namesurname;
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
}
