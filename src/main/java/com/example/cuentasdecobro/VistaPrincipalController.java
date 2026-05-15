package com.example.cuentasdecobro;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import javafx.collections.ObservableList;

public class VistaPrincipalController {

    @FXML private BorderPane rootPane;
    @FXML private Button btnCrearCuenta;
    @FXML private Button btnAdmin;
    @FXML private Button btnRevisor;
    @FXML private Button btnNuevaCuenta;
    @FXML private Label lblBadge;
    @FXML private Label lblUsuario;
    @FXML private TableView tblCuentas;
    @FXML private Label lblRegistros;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox cmbEstado;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;

    @FXML
    public void initialize() {
        Usuario u = SesionActual.get();
        if (u != null) {
            if (lblUsuario != null)
                lblUsuario.setText(u.getNombres() + "\n" + u.getRol().toUpperCase());

            if (btnAdmin != null)
                btnAdmin.setVisible(u.esAdmin());

            if (btnRevisor != null)
                btnRevisor.setVisible(u.esAdmin() || u.esRevisor());

            if (btnCrearCuenta != null)
                btnCrearCuenta.setVisible(u.esContratista());

            if (btnNuevaCuenta != null)
                btnNuevaCuenta.setVisible(u.esContratista());
        }

        if (lblBadge != null) {
            NotificacionService.getInstancia().badgeProperty().addListener(
                    (obs, old, nuevo) -> {
                        int n = nuevo.intValue();
                        lblBadge.setText(n > 0 ? String.valueOf(n) : "");
                        lblBadge.setVisible(n > 0);
                    });
        }

        cargarVista("mis_cuentas.fxml");
    }

    private void cargarVista(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/cuentasdecobro/" + fxml));
            Node vista = loader.load();
            FadeTransition fade = new FadeTransition(Duration.millis(250), vista);
            fade.setFromValue(0);
            fade.setToValue(1);
            rootPane.setCenter(vista);
            fade.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleInicio()        { cargarVista("mis_cuentas.fxml"); }
    @FXML private void handleCrearCuenta()   { cargarVista("informe.fxml"); }
    @FXML private void handleMisCuentas()    { cargarVista("mis_cuentas.fxml"); }
    @FXML private void handleBeneficiarios() { cargarVista("beneficiarios.fxml"); }
    @FXML private void handleReportes()      { cargarVista("reportes.fxml"); }
    @FXML private void handleConfiguracion() { cargarVista("configuracion.fxml"); }
    @FXML private void handleAdmin()         { cargarVista("admin.fxml"); }
    @FXML private void handleRevisor()       { cargarVista("revisor.fxml"); }

    @FXML
    private void verNotificaciones() {
        ObservableList<String> notifs =
                NotificacionService.getInstancia().getNotificaciones();
        if (notifs.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Sin notificaciones nuevas.").showAndWait();
        } else {
            StringBuilder sb = new StringBuilder();
            notifs.stream().limit(10).forEach(n -> sb.append("• ").append(n).append("\n"));
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Notificaciones");
            a.setHeaderText("Últimas notificaciones");
            a.setContentText(sb.toString());
            a.showAndWait();
            NotificacionService.getInstancia().limpiar();
        }
    }

    @FXML
    private void cerrarSesion() {
        SesionActual.cerrar();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/cuentasdecobro/login.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            javafx.stage.Stage stage = (javafx.stage.Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login - MiCCobro");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void filtrarCuentas() {}
    @FXML private void abrirFormulario() { cargarVista("informe.fxml"); }
}