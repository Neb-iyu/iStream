module com.istream.client {
    requires java.rmi;
    requires java.desktop;

    requires shared; 

    requires javafx.controls; 
    requires javafx.fxml;     
    requires javafx.graphics;  
    requires javafx.media;    
    requires javafx.swing;   
    opens com.istream.client to javafx.fxml;
    opens com.istream.client.controller to javafx.fxml;

    exports com.istream.client;
}
