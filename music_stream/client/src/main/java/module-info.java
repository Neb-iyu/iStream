module com.istream {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.rmi;
    requires shared;
    requires javafx.graphics;    

    opens com.istream to javafx.fxml;
    exports com.istream;
}
