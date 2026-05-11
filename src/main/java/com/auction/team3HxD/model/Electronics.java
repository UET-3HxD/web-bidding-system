package com.auction.team3HxD.model;

public class Electronics extends Item{
    private String brand;
    private int warrantyMonths;

    // Constructor
    public Electronics(String name , String description , double startingPrice) {
        super(name, description, startingPrice);
    }

    // Getter and setter
    public void setBrand(String brand) {this.brand = brand;}

    public String getBrand() {return brand;}

    public void setWarrantyMonths(int warrantyMonths) {this.warrantyMonths = warrantyMonths;}

    public int getWarrantyMonths() {return warrantyMonths;}
}
