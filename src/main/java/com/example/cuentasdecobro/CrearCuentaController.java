package com.example.cuentasdecobro;

import javafx.fxml.FXML;
import javafx.scene.control.*;
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
                txtValor.getText().isEmpty()   || cmbEstado.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Campos incompletos");
            alert.setContentText("Por favor completa todos los campos.");
            alert.showAndWait();
            return;
        }

        String sql = "INSERT INTO cuentas (numero_cuenta, funcionario_receptor, nombre_entidad, fecha_emision, valor, estado) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConexionSQLite();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtNumero.getText());
            ps.setString(2, txtFuncionario.getText());
            ps.setString(3, txtEntidad.getText());
            ps.setString(4, dpFecha.getValue().toString());
            ps.setString(5, txtValor.getText());
            ps.setString(6, cmbEstado.getValue());
            ps.executeUpdate();

            CuentaService.getInstancia().agregarCuenta(new Cuenta(
                    txtNumero.getText(), txtFuncionario.getText(),
                    txtEntidad.getText(), dpFecha.getValue(),
                    txtValor.getText(), cmbEstado.getValue()
            ));

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Exito");
            alert.setContentText("Cuenta guardada correctamente.");
            alert.showAndWait();
            limpiar();

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error al guardar: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML private void cancelar() { limpiar(); }

    private void limpiar() {
        txtNumero.clear();
        txtFuncionario.clear();
        txtEntidad.clear();
        dpFecha.setValue(null);
        txtValor.clear();
        cmbEstado.setValue(null);
    }
}