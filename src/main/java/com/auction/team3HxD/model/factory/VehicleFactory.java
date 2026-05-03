package com.auction.team3HxD.model.factory;

import com.auction.team3HxD.model.Item;
import com.auction.team3HxD.model.Vehicle;

public class VehicleFactory implements ItemFactory{
    public Item createItem(String name , String description , double startingPrice) {
        return new Vehicle(name , description , startingPrice);
    }
}
