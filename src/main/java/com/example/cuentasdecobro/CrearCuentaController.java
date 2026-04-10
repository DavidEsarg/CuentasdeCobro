package com.example.cuentasdecobro;

import javafx.fxml.FXML;
import javafx.scene.control.*;
// Librerías de SQL necesarias
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CrearCuentaController {

    @FXML private TextField txtNumero;
    @FXML private TextField txtFuncionario;
    @FXML private TextField txtEntidad;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtValor;
    @FXML private ComboBox<String> cmbEstado;

    @FXML
    public void initialize() {
        cmbEstado.getItems().addAll("Pendiente", "Aprobada", "Rechazada", "Pagada");
    }

    @FXML
    private void guardar() {
        if (txtNumero.getText().isEmpty() || txtFuncionario.getText().isEmpty() ||
                txtEntidad.getText().isEmpty() || dpFecha.getValue() == null ||
                txtValor.getText().isEmpty() || cmbEstado.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Campos incompletos");
            alert.setContentText("Por favor completa todos los campos.");
            alert.showAndWait();
            return;
        }

        // --- INICIO DE CONEXIÓN A BASE DE DATOS ---
        String sql = "INSERT INTO cuentas (numero_cuenta, funcionario_receptor, nombre_entidad, fecha_emision, valor, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txtNumero.getText());
            ps.setString(2, txtFuncionario.getText());
            ps.setString(3, txtEntidad.getText());
            ps.setDate(4, java.sql.Date.valueOf(dpFecha.getValue()));
            ps.setString(5, txtValor.getText());
            ps.setString(6, cmbEstado.getValue());

            ps.executeUpdate();
            // --- FIN DE CONEXIÓN A BASE DE DATOS ---

            // Mantengo tu lógica original de servicio por si la usas en la tabla
            Cuenta cuenta = new Cuenta(
                    txtNumero.getText(),
                    txtFuncionario.getText(),
                    txtEntidad.getText(),
                    dpFecha.getValue(),
                    txtValor.getText(),
                    cmbEstado.getValue()
            );
            CuentaService.getInstancia().agregarCuenta(cuenta);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setContentText("✅ Cuenta guardada correctamente en la Base de Datos.");
            alert.showAndWait();
            limpiar();

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error DB");
            alert.setContentText("❌ No se pudo guardar en la base de datos: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void cancelar() { limpiar(); }

    private void limpiar() {
        txtNumero.clear();
        txtFuncionario.clear();
        txtEntidad.clear();
        dpFecha.setValue(null);
        txtValor.clear();
        cmbEstado.setValue(null);
    }
}