module com.example.cuentasdecobro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires itextpdf;

    opens com.example.cuentasdecobro to javafx.fxml;
    exports com.example.cuentasdecobro;
}