module com.example.cuentasdecobro {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.cuentasdecobro to javafx.fxml;
    exports com.example.cuentasdecobro;
}