package com.auction.team3HxD.dto;

public class ProductDTO {

    String id, name, price, description, status, imagePath;

    public ProductDTO(String id, String name, String price, String desc, String status,
                   String imagePath) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = desc;
        this.status = status;
        this.imagePath = imagePath;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}