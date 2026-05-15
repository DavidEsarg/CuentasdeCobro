package com.example.cuentasdecobro;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class ConfiguracionController {

    @FXML private Label lblNombreUsuario;
    @FXML private Label lblNombreCompleto;
    @FXML private Label lblCedula;
    @FXML private PasswordField txtPasswordActual;
    @FXML private PasswordField txtPasswordNueva;
    @FXML private PasswordField txtPasswordConfirmar;
    @FXML private Label lblMensaje;

    @FXML
    public void initialize() {
        Usuario u = SesionActual.get();
        if (u != null) {
            lblNombreUsuario.setText(u.getNombreUsuario());
            lblNombreCompleto.setText(u.getNombres() + " " + u.getApellidos());
            lblCedula.setText(u.getCedula());
        }
    }

    @FXML
    private void cambiarPassword() {
        Usuario u = SesionActual.get();
        if (u == null) return;

        String actual    = txtPasswordActual.getText();
        String nueva     = txtPasswordNueva.getText();
        String confirmar = txtPasswordConfirmar.getText();

        if (actual.isBlank() || nueva.isBlank() || confirmar.isBlank()) {
            lblMensaje.setText("Completa todos los campos.");
            lblMensaje.setStyle("-fx-text-fill: red;");
            return;
        }
        if (!u.getPassword().equals(actual)) {
            lblMensaje.setText("La contraseña actual es incorrecta.");
            lblMensaje.setStyle("-fx-text-fill: red;");
            return;
        }
        if (!nueva.equals(confirmar)) {
            lblMensaje.setText("Las contraseñas nuevas no coinciden.");
            lblMensaje.setStyle("-fx-text-fill: red;");
            return;
        }
        if (nueva.length() < 6) {
            lblMensaje.setText("La contraseña debe tener al menos 6 caracteres.");
            lblMensaje.setStyle("-fx-text-fill: red;");
            return;
        }

        String sql = "UPDATE usuarios SET password=? WHERE nombre_usuario=?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nueva);
            ps.setString(2, u.getNombreUsuario());
            ps.executeUpdate();
            AppLogger.info(u.getNombreUsuario(), "CAMBIO_PASSWORD", "Contraseña actualizada.");
            txtPasswordActual.clear();
            txtPasswordNueva.clear();
            txtPasswordConfirmar.clear();
            lblMensaje.setText("Contraseña actualizada correctamente.");
            lblMensaje.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            lblMensaje.setText("Error: " + e.getMessage());
            lblMensaje.setStyle("-fx-text-fill: red;");
        }
    }
}