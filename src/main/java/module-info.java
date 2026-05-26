module com.auction.team3hxd {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;

    opens com.auction.team3hxd to javafx.fxml;
    opens com.auction.team3hxd.model to java.sql;
    opens com.auction.team3hxd.controller to javafx.fxml;
  
    exports com.auction.team3hxd;
}