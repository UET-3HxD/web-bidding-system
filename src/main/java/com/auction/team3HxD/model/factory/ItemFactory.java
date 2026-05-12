package com.auction.team3HxD.model.factory;

import com.auction.team3HxD.model.Item;

public interface ItemFactory {
    Item createItem(String name , String description , double startingPrice);
}
