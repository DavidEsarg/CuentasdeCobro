package com.example.cuentasdecobro;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

public class VistaPrincipalController {

    @FXML private BorderPane rootPane;
    @FXML private Button btnInicio;
    @FXML private Button btnCrearCuenta;
    @FXML private Button btnMisCuentas;
    @FXML private Button btnBeneficiarios;
    @FXML private Button btnReportes;
    @FXML private Button btnConfiguracion;
    @FXML private Button btnCerrarSesion;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox cmbEstado;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private Button btnBuscar;
    @FXML private Button btnNuevaCuenta;
    @FXML private TableView tblCuentas;
    @FXML private Label lblRegistros;

    private void cargarVista(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/cuentasdecobro/" + fxml));
            Node vista = loader.load();
            rootPane.setCenter(vista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleInicio()        { cargarVista("vista_principal.fxml"); }
    @FXML private void handleCrearCuenta()   { cargarVista("informe.fxml"); }
    @FXML private void handleMisCuentas()    { cargarVista("mis_cuentas.fxml"); }
    @FXML private void handleBeneficiarios() { cargarVista("beneficiarios.fxml"); }
    @FXML private void handleReportes()      { cargarVista("reportes.fxml"); }
    @FXML private void handleConfiguracion() { cargarVista("configuracion.fxml"); }

    @FXML private void cerrarSesion() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/cuentasdecobro/login.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            javafx.stage.Stage stage = (javafx.stage.Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login - Sistema de cuentas de cobro");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void filtrarCuentas() { System.out.println("Filtrando..."); }
    @FXML private void abrirFormulario() { cargarVista("informe.fxml"); }
}