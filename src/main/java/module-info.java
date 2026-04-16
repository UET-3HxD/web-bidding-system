module com.auction.team3HxD {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires mysql.connector.j;

    opens com.auction.team3HxD to javafx.fxml;
    opens com.auction.team3HxD.model to java.sql;
    exports com.auction.team3HxD;
}