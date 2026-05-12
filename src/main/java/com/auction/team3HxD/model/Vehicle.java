package com.auction.team3HxD.model;

public class Vehicle extends Item{
    private String licensePlate;
    private String model;
    private int year;

    //Constructor
    public Vehicle(String name , String description , double startingPrice) {
        super(name, description, startingPrice);
    }

    // Getter and setter
    public void setLicensePlate(String licensePlate) {this.licensePlate = licensePlate;}

    public String getLicensePlate() {return licensePlate;}

    public void setModel(String model) {this.model = model;}

    public String getModel() {return model;}

    public void setYear(int year) {this.year = year;}

    public int getYear() {return year;}
}
