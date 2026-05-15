package com.example.cuentasdecobro;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class CrearCuentaController {

    @FXML private TextField txtNumero;
    @FXML private TextField txtFuncionario;
    @FXML private TextField txtEntidad;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtValor;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Label lblMensaje;

    @FXML
    public void initialize() {
        cmbEstado.getItems().addAll("Pendiente", "Aprobada", "Rechazada", "Pagada");
        cmbEstado.setValue("Pendiente");
        int consecutivo = ValidadorColombia.obtenerSiguienteConsecutivo();
        txtNumero.setText(ValidadorColombia.generarNumeroCuenta(consecutivo));
        txtNumero.setEditable(false);
        txtNumero.setStyle("-fx-background-color: #f0f0f0;");
        if (SesionActual.get() != null) {
            txtFuncionario.setText(
                    SesionActual.get().getNombres() + " " + SesionActual.get().getApellidos());
        }
    }

    @FXML
    private void guardar() {
        if (txtFuncionario.getText().isBlank() || txtEntidad.getText().isBlank() ||
                dpFecha.getValue() == null || txtValor.getText().isBlank()) {
            lblMensaje.setText("Completa todos los campos.");
            lblMensaje.setStyle("-fx-text-fill: red;");
            return;
        }

        String sql = """
            INSERT INTO cuentas
            (numero_cuenta, funcionario_receptor, nombre_entidad,
             fecha_emision, valor, estado)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
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
                    txtValor.getText(), cmbEstado.getValue()));

            HistorialService.registrarCambio(txtNumero.getText(), "CREACION",
                    "Cuenta creada por " + (SesionActual.get() != null ?
                            SesionActual.get().getNombreUsuario() : "sistema"),
                    SesionActual.get() != null ?
                            SesionActual.get().getNombreUsuario() : "sistema");

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("Cuenta " + txtNumero.getText() + " creada correctamente.");
            a.showAndWait();
            limpiar();
        } catch (Exception e) {
            lblMensaje.setText("Error: " + e.getMessage());
            lblMensaje.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML private void cancelar() { limpiar(); }

    private void limpiar() {
        int consecutivo = ValidadorColombia.obtenerSiguienteConsecutivo();
        txtNumero.setText(ValidadorColombia.generarNumeroCuenta(consecutivo));
        txtEntidad.clear();
        dpFecha.setValue(null);
        txtValor.clear();
        cmbEstado.setValue("Pendiente");
        lblMensaje.setText("");
    }
}