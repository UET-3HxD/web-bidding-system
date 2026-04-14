module com.auction.team3HxD {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.auction.team3HxD.controller to javafx.fxml;
    exports com.auction.team3HxD;
}