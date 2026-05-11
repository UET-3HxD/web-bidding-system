package com.auction.team3HxD.model;

public class Art extends Item{
    private String artist;
    private int year;

    // Constructor
    public Art(String name , String description , double startingPrice) {
        super(name , description , startingPrice);
    }

    // Getter and setter
    public void setArtist(String artist) {this.artist = artist;}

    public String getArtist() {return artist;}

    public void setYear(int year) {this.year = year;}

    public int getYear() {return year;}
}
