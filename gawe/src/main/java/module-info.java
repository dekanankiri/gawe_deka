module id.ac.stis.pbo.demo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.desktop;
    requires java.sql;

    exports id.ac.stis.pbo.demo1;
    exports id.ac.stis.pbo.demo1.models;
    exports id.ac.stis.pbo.demo1.data;
    exports id.ac.stis.pbo.demo1.server;
    exports id.ac.stis.pbo.demo1.ui;
}
