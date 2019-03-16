package com.example.nicat.travelgram.Model;

public class Post {
    private String postid;
    private String photourl;
    private String description;
    private String publisher;
    private String photodate;

    public Post(String postid, String photourl, String description, String publisher, String photodate) {
        this.postid = postid;
        this.photourl = photourl;
        this.description = description;
        this.publisher = publisher;
        this.photodate = photodate;
    }
    
    public Post(){
        
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPhotodate() {
        return photodate;
    }

    public void setPhotodate(String photodate) {
        this.photodate = photodate;
    }
}
