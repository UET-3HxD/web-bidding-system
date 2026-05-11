package com.auction.team3HxD.model;

import com.auction.team3HxD.exception.AuctionClosedException;
import com.auction.team3HxD.exception.IllegalItemChangeException;
import com.auction.team3HxD.model.enums.AuctionStatus;
import com.auction.team3HxD.model.enums.Role;

import java.util.List;

public class Seller extends User{
    private List<Item> sellingItems;

    //Constructor
    public Seller(String userName , String passwordHash , String email , Role role) {
        super(userName , passwordHash , email , role);
    }

    // Thêm sản phẩm
    public void addItem(Item item) {sellingItems.add(item);}

    // Cập nhật sản phẩm
    public void updateItem(Item item , String name , String description) throws IllegalItemChangeException, AuctionClosedException {
        if (item.getStatus() == AuctionStatus.RUNNING) {
            throw new IllegalItemChangeException("Cannot update item while RUNNING");
        }

        if (item.getStatus() != AuctionStatus.OPEN) {
            throw new AuctionClosedException("Auction closed");
        }

        item.setName(name);
        item.setDescription(description);
    }

    // Xóa sản phẩm
    public void removeItem(Item item) throws IllegalItemChangeException, AuctionClosedException{
        if (item.getStatus() == AuctionStatus.RUNNING) {
            throw new IllegalItemChangeException("Cannot remove item while RUNNING");
        }

        if (item.getStatus() != AuctionStatus.OPEN) {
            throw new AuctionClosedException("Auction closed");
        }

        sellingItems.remove(item);
    }
}
