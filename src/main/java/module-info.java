module org.example.transportapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires gs.core;
    requires gs.ui.javafx;
    requires javafx.graphics;

    exports org.example.transportapp.view;
    exports org.example.transportapp.model;
    exports org.example.transportapp.graph;
    opens org.example.transportapp to javafx.fxml;
//    opens org.example.transportapp.model to com.fasterxml.jackson.databind;
    opens org.example.transportapp.model to javafx.base;
}