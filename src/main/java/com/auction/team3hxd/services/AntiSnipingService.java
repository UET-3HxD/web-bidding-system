package com.auction.team3hxd.services;

import java.sql.Connection;
import java.sql.SQLException;

import com.auction.team3hxd.dao.AuctionDAO;

public class AntiSnipingService {
    public boolean processAntiSnipe(Connection conn, AuctionDAO dao, int auctionId, int timeLeft)
            throws SQLException {
        if (timeLeft > 0 && timeLeft <= 30) {
            dao.extendEndTime(conn, auctionId);
            return true;
        }
        return false;
    }
}
