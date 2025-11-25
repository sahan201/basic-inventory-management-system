module com.example.dummy_inventory {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires com.opencsv;

    opens com.example.dummy_inventory.controller to javafx.fxml;

    opens com.example.dummy_inventory to javafx.fxml;

    exports com.example.dummy_inventory;
}