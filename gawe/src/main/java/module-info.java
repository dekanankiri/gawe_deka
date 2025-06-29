module id.ac.stis.pbo.gawe {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.desktop;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires org.slf4j;
    requires java.logging;

    exports id.ac.stis.pbo.demo1;
    exports id.ac.stis.pbo.demo1.models;
    exports id.ac.stis.pbo.demo1.data;
    exports id.ac.stis.pbo.demo1.server;
    exports id.ac.stis.pbo.demo1.ui;
    exports id.ac.stis.pbo.demo1.database;

    opens id.ac.stis.pbo.demo1.models;
    opens id.ac.stis.pbo.demo1.data;
    opens id.ac.stis.pbo.demo1.server;
    opens id.ac.stis.pbo.demo1.database;
}
