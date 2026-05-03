package com.auction.team3HxD.model.factory;

import com.auction.team3HxD.model.Electronics;
import com.auction.team3HxD.model.Item;

public class ElectronicsFactory implements ItemFactory{
    public Item createItem(String name , String description , double startingPrice) {
        return new Electronics(name , description , startingPrice);
    }
}
