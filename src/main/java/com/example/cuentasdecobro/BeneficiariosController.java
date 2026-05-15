package com.example.cuentasdecobro;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class BeneficiariosController {

    @FXML private TableView<Beneficiario> tblBeneficiarios;
    @FXML private TableColumn<Beneficiario, String> colNombre;
    @FXML private TableColumn<Beneficiario, String> colCedula;
    @FXML private TableColumn<Beneficiario, String> colBanco;
    @FXML private TableColumn<Beneficiario, String> colTipo;
    @FXML private TableColumn<Beneficiario, String> colCuenta;
    @FXML private TableColumn<Beneficiario, String> colEmail;
    @FXML private TableColumn<Beneficiario, String> colTelefono;

    @FXML private TextField txtNombre;
    @FXML private TextField txtCedula;
    @FXML private ComboBox<String> cmbBanco;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtNumeroCuenta;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtBuscar;
    @FXML private Label lblMensaje;

    private ObservableList<Beneficiario> lista = FXCollections.observableArrayList();
    private Beneficiario seleccionado = null;

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getNombre()));
        colCedula.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getCedula()));
        colBanco.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getBanco()));
        colTipo.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getTipoCuenta()));
        colCuenta.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getNumeroCuenta()));
        colEmail.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getEmail()));
        colTelefono.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getTelefono()));

        cmbBanco.getItems().addAll("Bancolombia", "Davivienda", "BBVA",
                "Banco de Bogotá", "Nequi", "Daviplata", "Otro");
        cmbTipo.getItems().addAll("Ahorros", "Corriente");

        tblBeneficiarios.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, nuevo) -> {
                    if (nuevo != null) cargarFormulario(nuevo);
                });

        txtBuscar.textProperty().addListener((obs, old, nuevo) -> buscarEnTiempoReal(nuevo));

        cargarBeneficiarios();
    }

    private void cargarBeneficiarios() {
        lista.clear();
        String sql = "SELECT * FROM beneficiarios ORDER BY nombre";
        try (Connection con = ConexionDB.getConexionSQLite();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Beneficiario(
                        rs.getInt("id"), rs.getString("nombre"),
                        rs.getString("cedula"), rs.getString("banco"),
                        rs.getString("tipo_cuenta"), rs.getString("numero_cuenta"),
                        rs.getString("email"), rs.getString("telefono")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        tblBeneficiarios.setItems(lista);
    }

    private void buscarEnTiempoReal(String texto) {
        if (texto == null || texto.isBlank()) {
            tblBeneficiarios.setItems(lista);
            return;
        }
        String lower = texto.toLowerCase();
        ObservableList<Beneficiario> filtrados = FXCollections.observableArrayList();
        for (Beneficiario b : lista) {
            if (b.getNombre().toLowerCase().contains(lower) ||
                    b.getCedula().contains(lower) ||
                    b.getBanco().toLowerCase().contains(lower))
                filtrados.add(b);
        }
        tblBeneficiarios.setItems(filtrados);
    }

    @FXML
    private void guardar() {
        if (!validar()) return;
        if (seleccionado == null) insertar();
        else actualizar();
    }

    private boolean validar() {
        if (txtNombre.getText().isBlank()) {
            lblMensaje.setText("El nombre es obligatorio.");
            return false;
        }
        if (!ValidadorColombia.esCedulaValida(txtCedula.getText())) {
            lblMensaje.setText("Cédula inválida (6-10 dígitos).");
            return false;
        }
        if (!ValidadorColombia.esTelefonoValido(txtTelefono.getText())) {
            lblMensaje.setText("Teléfono inválido (10 dígitos, inicia en 3).");
            return false;
        }
        if (!ValidadorColombia.esEmailValido(txtEmail.getText())) {
            lblMensaje.setText("Email inválido.");
            return false;
        }
        if (cmbBanco.getValue() == null || cmbTipo.getValue() == null) {
            lblMensaje.setText("Selecciona banco y tipo de cuenta.");
            return false;
        }
        lblMensaje.setText("");
        return true;
    }

    private void insertar() {
        String sql = """
            INSERT INTO beneficiarios
            (nombre, cedula, banco, tipo_cuenta, numero_cuenta, email, telefono)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection con = ConexionDB.getConexionSQLite();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtNombre.getText());
            ps.setString(2, ValidadorColombia.formatearCedula(txtCedula.getText()));
            ps.setString(3, cmbBanco.getValue());
            ps.setString(4, cmbTipo.getValue());
            ps.setString(5, txtNumeroCuenta.getText());
            ps.setString(6, txtEmail.getText());
            ps.setString(7, txtTelefono.getText());
            ps.executeUpdate();
            mostrarExito("Beneficiario guardado.");
            limpiar();
            cargarBeneficiarios();
        } catch (Exception e) {
            lblMensaje.setText("Error: " + e.getMessage());
        }
    }

    private void actualizar() {
        String sql = """
            UPDATE beneficiarios SET nombre=?, cedula=?, banco=?,
            tipo_cuenta=?, numero_cuenta=?, email=?, telefono=?
            WHERE id=?
        """;
        try (Connection con = ConexionDB.getConexionSQLite();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, txtNombre.getText());
            ps.setString(2, ValidadorColombia.formatearCedula(txtCedula.getText()));
            ps.setString(3, cmbBanco.getValue());
            ps.setString(4, cmbTipo.getValue());
            ps.setString(5, txtNumeroCuenta.getText());
            ps.setString(6, txtEmail.getText());
            ps.setString(7, txtTelefono.getText());
            ps.setInt(8, seleccionado.getId());
            ps.executeUpdate();
            mostrarExito("Beneficiario actualizado.");
            limpiar();
            cargarBeneficiarios();
        } catch (Exception e) {
            lblMensaje.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        Beneficiario b = tblBeneficiarios.getSelectionModel().getSelectedItem();
        if (b == null) { lblMensaje.setText("Selecciona un beneficiario."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar a " + b.getNombre() + "?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try (Connection con = ConexionDB.getConexionSQLite();
                     PreparedStatement ps = con.prepareStatement(
                             "DELETE FROM beneficiarios WHERE id=?")) {
                    ps.setInt(1, b.getId());
                    ps.executeUpdate();
                    mostrarExito("Beneficiario eliminado.");
                    limpiar();
                    cargarBeneficiarios();
                } catch (Exception e) {
                    lblMensaje.setText("Error: " + e.getMessage());
                }
            }
        });
    }

    @FXML private void nuevo() { limpiar(); }

    private void cargarFormulario(Beneficiario b) {
        seleccionado = b;
        txtNombre.setText(b.getNombre());
        txtCedula.setText(b.getCedula());
        cmbBanco.setValue(b.getBanco());
        cmbTipo.setValue(b.getTipoCuenta());
        txtNumeroCuenta.setText(b.getNumeroCuenta());
        txtEmail.setText(b.getEmail() != null ? b.getEmail() : "");
        txtTelefono.setText(b.getTelefono() != null ? b.getTelefono() : "");
    }

    private void limpiar() {
        seleccionado = null;
        txtNombre.clear(); txtCedula.clear();
        cmbBanco.setValue(null); cmbTipo.setValue(null);
        txtNumeroCuenta.clear(); txtEmail.clear(); txtTelefono.clear();
        lblMensaje.setText("");
        tblBeneficiarios.getSelectionModel().clearSelection();
    }

    private void mostrarExito(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg); a.showAndWait();
    }
}