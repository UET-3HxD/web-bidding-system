module com.auction.team3HxD {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.auction.team3HxD to javafx.fxml;
    exports com.auction.team3HxD;
}