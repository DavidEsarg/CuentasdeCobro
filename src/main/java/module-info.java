module com.example.cuentasdecobro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires itextpdf;

    // MongoDB
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;

    opens com.example.cuentasdecobro to javafx.fxml;
    exports com.example.cuentasdecobro;
}