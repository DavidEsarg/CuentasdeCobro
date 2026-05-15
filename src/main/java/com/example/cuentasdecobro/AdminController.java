package com.example.cuentasdecobro;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class AdminController {

    @FXML private TableView<UsuarioFila> tblUsuarios;
    @FXML private TableColumn<UsuarioFila, String> colUsuario;
    @FXML private TableColumn<UsuarioFila, String> colNombres;
    @FXML private TableColumn<UsuarioFila, String> colApellidos;
    @FXML private TableColumn<UsuarioFila, String> colCedula;
    @FXML private TableColumn<UsuarioFila, String> colCuentas;
    @FXML private Label lblTotalUsuarios;
    @FXML private Label lblTotalCuentas;

    @FXML
    public void initialize() {
        colUsuario.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().usuario));
        colNombres.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().nombres));
        colApellidos.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().apellidos));
        colCedula.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().cedula));
        colCuentas.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().totalCuentas));
        cargarDatos();
    }

    private void cargarDatos() {
        ObservableList<UsuarioFila> lista = FXCollections.observableArrayList();
        String sql = """
            SELECT u.nombre_usuario, u.nombres, u.apellidos, u.cedula,
                   COUNT(c.id) as total_cuentas
            FROM usuarios u
            LEFT JOIN cuentas c ON c.funcionario_receptor = u.nombres || ' ' || u.apellidos
            GROUP BY u.nombre_usuario
        """;
        try (Connection con = ConexionDB.getConexionSQLite();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new UsuarioFila(
                        rs.getString("nombre_usuario"),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("cedula"),
                        String.valueOf(rs.getInt("total_cuentas"))
                ));
            }
            lblTotalUsuarios.setText("Total usuarios: " + lista.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        int totalCuentas = 0;
        try (Connection con = ConexionDB.getConexionSQLite();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) as total FROM cuentas")) {
            if (rs.next()) totalCuentas = rs.getInt("total");
        } catch (Exception e) {
            e.printStackTrace();
        }

        tblUsuarios.setItems(lista);
        lblTotalCuentas.setText("Total cuentas: " + totalCuentas);
    }

    @FXML
    private void actualizar() { cargarDatos(); }

    public static class UsuarioFila {
        String usuario, nombres, apellidos, cedula, totalCuentas;
        UsuarioFila(String u, String n, String a, String c, String t) {
            usuario = u; nombres = n; apellidos = a; cedula = c; totalCuentas = t;
        }
    }
}