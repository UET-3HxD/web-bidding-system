package com.auction.team3HxD.model;

import java.util.UUID;

public class NormalItem extends Item {

    public NormalItem(String name, String description, double startingPrice) {
        super(name, description, startingPrice);
    }

    // Constructor dùng cho DB
    public NormalItem(UUID id, String name, String description,
                      double startingPrice, String imageURL, Seller owner) {

        super(name, description, startingPrice);
        this.setId(id);
        this.setImageURL(imageURL);
        this.setOwner(owner);
    }
}