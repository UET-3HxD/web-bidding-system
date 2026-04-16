package com.auction.team3HxD.model;

public abstract class Item extends Entity{
    private String name;
    private String description;
    private double startingPrice;
    private String imageURL;
    private Seller owner;

    //Getter and setter
    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}

    public double getStartingPrice() {return startingPrice;}

    public void setStartingPrice(double startingPrice) {this.startingPrice = startingPrice;}

    //Constructor
    public Item(String name , String description , double startingPrice) {
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
    }
}
