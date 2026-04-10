package com.example.cuentasdecobro;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

// IMPORTANTE: Agregamos las librerías para la conexión
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMensaje;

    @FXML
    private void login() {
        String user = txtUsuario.getText();
        String pass = txtPassword.getText();

        // 1. Validamos contra la base de datos MySQL
        if (validarAccesoEnBD(user, pass)) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(
                                "/com/example/cuentasdecobro/vista_principal.fxml")
                );
                Scene scene = new Scene(loader.load());
                Stage stage = (Stage) txtUsuario.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Sistema de cuentas de cobro");
            } catch (Exception e) {
                e.printStackTrace();
                lblMensaje.setText("❌ Error al cargar la vista principal");
            }
        } else {
            lblMensaje.setText("❌ Usuario o contraseña incorrectos");
        }
    }

    // NUEVO MÉTODO: Consulta real a MySQL
    private boolean validarAccesoEnBD(String user, String pass) {
        String sql = "SELECT * FROM usuarios WHERE nombre_usuario = ? AND password = ?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user);
            ps.setString(2, pass);

            ResultSet rs = ps.executeQuery();

            // Si encuentra una fila, los datos son correctos
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void irARegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/example/cuentasdecobro/registro.fxml")
            );
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}