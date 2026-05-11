package com.auction.team3HxD.model.factory;

import com.auction.team3HxD.model.Art;
import com.auction.team3HxD.model.Item;

public class ArtFactory implements ItemFactory{
    public Item createItem(String name , String description , double startingPrice) {
        return new Art(name , description , startingPrice);
    }
}
