module com.istream.client {
    // Standard Java modules
    requires java.rmi;

    // Project-specific modules
    requires shared; // Assumes your shared library is a module named 'shared'
                     // and it exports the 'com.istream.rmi' package.

    // JavaFX modules
    requires javafx.controls; // For Alert, Button, etc.
    requires javafx.fxml;     // For FXMLLoader and FXML integration
    requires javafx.graphics;  // For Application, Stage, Scene, Parent, etc.
    requires javafx.media;    // If any part of your client uses media playback

    // Open packages to JavaFX FXML for reflection if they contain FXML controllers
    // or are referenced directly in FXML files.
    opens com.istream.client to javafx.fxml;
    opens com.istream.client.controller to javafx.fxml;
    // Add other 'opens' directives if other client packages need to be accessed by JavaFX FXML.

    // Export your client's main package if it needs to be used by other modules (usually not needed for a main application module)
    exports com.istream.client;
}
