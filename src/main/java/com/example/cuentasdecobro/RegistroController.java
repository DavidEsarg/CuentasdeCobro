package com.example.cuentasdecobro;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

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
    @FXML private ComboBox<String> cmbRol;

    private ObservableList<String> entidades = FXCollections.observableArrayList();
    private ObservableList<String> contratos  = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        lstEntidades.setItems(entidades);
        lstContratos.setItems(contratos);
        cmbRol.getItems().addAll("contratista", "revisor");
        cmbRol.setValue("contratista");
    }

    @FXML
    private void agregarEntidad() {
        String e = txtEntidad.getText().trim();
        if (!e.isEmpty()) { entidades.add(e); txtEntidad.clear(); }
    }

    @FXML
    private void agregarContrato() {
        String c = txtContrato.getText().trim();
        if (!c.isEmpty()) { contratos.add(c); txtContrato.clear(); }
    }

    @FXML
    private void registrar() {
        if (txtNombres.getText().isEmpty() || txtApellidos.getText().isEmpty() ||
                txtCedula.getText().isEmpty()  || txtUsuario.getText().isEmpty()   ||
                txtPassword.getText().isEmpty()|| entidades.isEmpty() || contratos.isEmpty()) {
            lblMensaje.setText("Completa todos los campos.");
            return;
        }
        if (!ValidadorColombia.esCedulaValida(txtCedula.getText())) {
            lblMensaje.setText("Cédula inválida (6-10 dígitos).");
            return;
        }
        if (!txtPassword.getText().equals(txtConfirmar.getText())) {
            lblMensaje.setText("Las contraseñas no coinciden.");
            return;
        }

        String rol = cmbRol.getValue() != null ? cmbRol.getValue() : "contratista";

        String sql = """
            INSERT INTO usuarios
            (nombre_usuario, password, nombres, apellidos, cedula, rol)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtUsuario.getText());
            ps.setString(2, txtPassword.getText());
            ps.setString(3, txtNombres.getText());
            ps.setString(4, txtApellidos.getText());
            ps.setString(5, ValidadorColombia.formatearCedula(txtCedula.getText()));
            ps.setString(6, rol);
            ps.executeUpdate();

            UsuarioService.getInstancia().registrar(new Usuario(
                    txtUsuario.getText(), txtPassword.getText(),
                    txtNombres.getText(), txtApellidos.getText(),
                    txtCedula.getText(),
                    new ArrayList<>(entidades),
                    new ArrayList<>(contratos),
                    rol
            ));

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setContentText("Usuario registrado como " + rol + ". Ya puedes iniciar sesión.");
            alert.showAndWait();
            volverLogin();

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                lblMensaje.setText("El nombre de usuario ya existe. Elige otro.");
            } else {
                lblMensaje.setText("Error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void volverLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/cuentasdecobro/login.fxml"));
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Login - MiCCobro");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}