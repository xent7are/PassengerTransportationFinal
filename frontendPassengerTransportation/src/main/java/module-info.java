module com.example.frontendpassengertransportation {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.google.gson;
    requires java.sql;

    exports com.example.frontendpassengertransportation.application;
    opens com.example.frontendpassengertransportation.model to com.google.gson, javafx.base;

    opens com.example.frontendpassengertransportation.application to javafx.fxml;
    opens com.example.frontendpassengertransportation.controller to javafx.fxml;
}