package com.example.cuentasdecobro;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMensaje;
    @FXML private Button btnIngresar;

    private int intentosFallidos = 0;
    private static final int MAX_INTENTOS = 5;

    @FXML
    private void login() {
        String user = txtUsuario.getText().trim();
        String pass = txtPassword.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            lblMensaje.setText("Ingresa usuario y contraseña.");
            return;
        }
        if (intentosFallidos >= MAX_INTENTOS) {
            lblMensaje.setText("Demasiados intentos. Reinicia la aplicación.");
            if (btnIngresar != null) btnIngresar.setDisable(true);
            return;
        }

        if (validarAcceso(user, pass)) {
            intentosFallidos = 0;
            AppLogger.info(user, "LOGIN", "Inicio de sesion exitoso.");
            Usuario u = SesionActual.get();
            String titulo = "Sistema de Cuentas de Cobro - " +
                    u.getNombres() + " " + u.getApellidos() +
                    " [" + u.getRol().toUpperCase() + "]";
            cargarFxml("vista_principal.fxml", titulo);
        } else {
            intentosFallidos++;
            AppLogger.error(user, "LOGIN_FALLIDO", "Intento #" + intentosFallidos);
            int restantes = MAX_INTENTOS - intentosFallidos;
            lblMensaje.setText(restantes > 0
                    ? "Credenciales incorrectas. Intentos restantes: " + restantes
                    : "Demasiados intentos. Reinicia la aplicación.");
            if (restantes <= 0 && btnIngresar != null) btnIngresar.setDisable(true);
        }
    }

    private boolean validarAcceso(String user, String pass) {
        Usuario u = UsuarioService.getInstancia().autenticar(user, pass);
        if (u != null) { SesionActual.set(u); return true; }

        String sql = "SELECT * FROM usuarios WHERE nombre_usuario=? AND password=?";
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Usuario nuevo = new Usuario(
                        rs.getString("nombre_usuario"),
                        rs.getString("password"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("cedula"),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        rs.getString("rol")
                );
                UsuarioService.getInstancia().registrar(nuevo);
                SesionActual.set(nuevo);
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    @FXML private void irARegistro() {
        cargarFxml("registro.fxml", "Registro de usuario");
    }

    private void cargarFxml(String nombreFxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/cuentasdecobro/" + nombreFxml));
            Scene scene = new Scene(loader.load());
            try {
                URL css = getClass().getResource("/com/example/cuentasdecobro/estilos.css");
                if (css != null) scene.getStylesheets().add(css.toExternalForm());
            } catch (Exception ignored) {}
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(titulo);
        } catch (Exception e) {
            e.printStackTrace();
            lblMensaje.setText("Error: " + e.getMessage());
        }
    }
}