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

    // Allow access to unnamed modules (jbcrypt, opencsv, apache poi)
    // NOTE: jbcrypt is commented out because org.mindrot:jbcrypt:0.4 is not a proper Java module
    // and does not expose a module name. The --add-reads compiler flag handles this instead.
    // See pom.xml: <arg>--add-reads</arg> <arg>com.example.dummy_inventory=ALL-UNNAMED</arg>
    // requires jbcrypt;

    requires org.apache.poi.poi;

    requires org.apache.poi.ooxml;

    // Export main package
    exports com.example.dummy_inventory;

    // Open specific packages for JavaFX reflection (FXML controllers)
    opens com.example.dummy_inventory.controller to javafx.fxml;
    opens com.example.dummy_inventory.model to javafx.base;

    // Export specific packages if needed by other modules
    exports com.example.dummy_inventory.controller;
    exports com.example.dummy_inventory.model;
    exports com.example.dummy_inventory.dao;
    exports com.example.dummy_inventory.util;
}