package com.example.cuentasdecobro;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.ArrayList;
// IMPORTANTE: Agregamos estas librerías para MySQL
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistroController {

    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtCedula;
    @FXML private TextField txtEntidad;
    @FXML private ListView<String> lstEntidades;
    @FXML private TextField txtContrato;
    @FXML private ListView<String> lstContratos;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmar;
    @FXML private Label lblMensaje;

    private ObservableList<String> entidades = FXCollections.observableArrayList();
    private ObservableList<String> contratos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        lstEntidades.setItems(entidades);
        lstContratos.setItems(contratos);
    }

    @FXML
    private void agregarEntidad() {
        String entidad = txtEntidad.getText().trim();
        if (!entidad.isEmpty()) {
            entidades.add(entidad);
            txtEntidad.clear();
        }
    }

    @FXML
    private void agregarContrato() {
        String contrato = txtContrato.getText().trim();
        if (!contrato.isEmpty()) {
            contratos.add(contrato);
            txtContrato.clear();
        }
    }

    @FXML
    private void registrar() {
        if (txtNombres.getText().isEmpty() || txtApellidos.getText().isEmpty() ||
                txtCedula.getText().isEmpty() || txtUsuario.getText().isEmpty() ||
                txtPassword.getText().isEmpty() || entidades.isEmpty() ||
                contratos.isEmpty()) {
            lblMensaje.setText("❌ Completa todos los campos.");
            return;
        }

        if (!txtPassword.getText().equals(txtConfirmar.getText())) {
            lblMensaje.setText("❌ Las contraseñas no coinciden.");
            return;
        }

        // --- INICIO DE CONEXIÓN A BASE DE DATOS ---
        String sql = "INSERT INTO usuarios (nombre_usuario, password, nombres, apellidos, cedula) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtUsuario.getText());
            ps.setString(2, txtPassword.getText());
            ps.setString(3, txtNombres.getText());
            ps.setString(4, txtApellidos.getText());
            ps.setString(5, txtCedula.getText());

            ps.executeUpdate();
            // --- FIN DE CONEXIÓN A BASE DE DATOS ---

            Usuario nuevo = new Usuario(
                    txtUsuario.getText(),
                    txtPassword.getText(),
                    txtNombres.getText(),
                    txtApellidos.getText(),
                    txtCedula.getText(),
                    new ArrayList<>(entidades),
                    new ArrayList<>(contratos)
            );

            UsuarioService.getInstancia().registrar(nuevo);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setContentText("✅ Usuario guardado en la Base de Datos. Ya puedes iniciar sesión.");
            alert.showAndWait();
            volverLogin();

        } catch (SQLException e) {
            e.printStackTrace();
            lblMensaje.setText("❌ Error al guardar en DB: " + e.getMessage());
        }
    }

    @FXML
    private void volverLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/example/cuentasdecobro/login.fxml")
            );
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}