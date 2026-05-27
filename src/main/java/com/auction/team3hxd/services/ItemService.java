package com.auction.team3hxd.services;

import com.auction.team3hxd.dao.ItemDAO;
import com.auction.team3hxd.dao.UserDAO;
import com.auction.team3hxd.model.*;

import java.util.List;

public class ItemService {
    private final ItemDAO itemDAO = new ItemDAO();
    private final UserDAO userDAO = new UserDAO();
    // Xử lý tạo sản phẩm dựa trên loại (Polymorphism)
    public int createItem(int sellerId, String name, double price, String type, String desc, String path) {
        Item newItem;
        switch (type.toUpperCase()) {
            case "ELECTRONIC":
                newItem = new Electronic(sellerId, name, desc, price, path);
                break;
            case "ART":
                newItem = new Art(sellerId, name, desc, price, path);
                break;
            case "VEHICLE":
                newItem = new Vehicle(sellerId, name, desc, price, path);
                break;
            default:
                return -1;
        }
        return itemDAO.saveItem(newItem, type);
    }
    // Lấy danh sách sản phẩm và định dạng thành chuỗi phản hồi cho Socket
    public String getMyItemsList(int sellerId) {
        List<Item> items = itemDAO.getAllItemsBySeller(sellerId);
        if (items.isEmpty()) return "LIST_ITEMS_EMPTY";

        StringBuilder sb = new StringBuilder("LIST_ITEMS_SUCCESS");
        for (Item item : items) {
            sb.append("|").append(item.getId())
                    .append("#").append(item.getName())
                    .append("#").append(item.getPrice())
                    .append("#").append(item.getStatus())
                    .append("#").append(item.getImagePath())
                    .append("#").append(item.getDescription());
        }
        return sb.toString();
    }
    public int updateItem(int itemId, String name, double price, String desc) {
        return itemDAO.updateItemInfo(itemId, name, price, desc);
    }
    public boolean deleteItem(int itemId) {
        return itemDAO.deleteItem(itemId);
    }
    public String getPendingItemsList() {
        List<Item> items = itemDAO.getItemsByStatus("WAITING");
        if (items == null || items.isEmpty()) {
            return "PENDING_ITEMS_EMPTY";
        }
        StringBuilder sb = new StringBuilder("PENDING_ITEMS_SUCCESS");
        for (Item item : items) {
            // Lấy tên người bán từ UserDAO
            User seller = userDAO.getUserById(item.getSellerId());
            String sellerName = seller != null ? seller.getUsername() : "Unknown";

            sb.append("|").append(item.getId()).append("#")
                    .append(item.getName()).append("#")
                    .append(item.getPrice()).append("#")
                    .append(sellerName).append("#")
                    .append(item.getDescription() != null ? item.getDescription() : "").append("#")
                    .append(item.getImagePath() != null ? item.getImagePath() : "");
        }
        return sb.toString();
    }
    public boolean itemValidator(String name, String price, String desc, String category) {
        if (name.isEmpty() || price.isEmpty() || desc.isEmpty() || category == null) {
            return false;
        }
        if (name.contains("#") || price.contains("#") || desc.contains("#") || category.contains("#")) {
            return false;
        }
        if (name.contains("|") || price.contains("|") || desc.contains("|") || category.contains("|")) {
            return false;
        }
        try {
            Double.parseDouble(price);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public boolean approveItem(int itemId) {
        return itemDAO.updateItemStatus(itemId, "APPROVED");
    }

    public boolean rejectItem(int itemId) {
        return itemDAO.updateItemStatus(itemId, "REJECTED");
    }
}
