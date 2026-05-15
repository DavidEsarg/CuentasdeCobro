package com.example.cuentasdecobro;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RevisorController {

    @FXML private TableView<Cuenta> tblCuentas;
    @FXML private TableColumn<Cuenta, String> colNumero;
    @FXML private TableColumn<Cuenta, String> colContratista;
    @FXML private TableColumn<Cuenta, String> colEntidad;
    @FXML private TableColumn<Cuenta, String> colFecha;
    @FXML private TableColumn<Cuenta, String> colValor;
    @FXML private TableColumn<Cuenta, String> colEstado;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;
    @FXML private VBox panelDetalle;
    @FXML private Label lblNumeroCuenta;
    @FXML private Label lblContratista;
    @FXML private Label lblEntidad;
    @FXML private Label lblFecha;
    @FXML private Label lblValor;
    @FXML private ComboBox<String> cmbNuevoEstado;
    @FXML private TextArea txtComentario;
    @FXML private ListView<String> lstComentarios;

    private ObservableList<Cuenta> todasLasCuentas = FXCollections.observableArrayList();
    private Cuenta cuentaSeleccionada = null;

    @FXML
    public void initialize() {
        colNumero.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getNumero()));
        colContratista.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getFuncionario()));
        colEntidad.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getEntidad()));
        colFecha.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getFecha().toString()));
        colValor.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getValor()));
        colEstado.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getEstado()));

        // Colorear filas por estado
        tblCuentas.setRowFactory(tv -> new TableRow<Cuenta>() {
            @Override
            protected void updateItem(Cuenta c, boolean empty) {
                super.updateItem(c, empty);
                if (c == null || empty) {
                    setStyle("");
                } else {
                    switch (c.getEstado()) {
                        case "Aprobada" ->
                                setStyle("-fx-background-color: #D5F5E3;");
                        case "Rechazada" ->
                                setStyle("-fx-background-color: #FADBD8;");
                        case "Pendiente" ->
                                setStyle("-fx-background-color: #FEF9E7;");
                        default -> setStyle("");
                    }
                }
            }
        });

        cmbFiltroEstado.getItems().addAll("Todos","Pendiente","Aprobada","Rechazada","Pagada");
        cmbFiltroEstado.setValue("Todos");
        cmbNuevoEstado.getItems().addAll("Pendiente","Aprobada","Rechazada","Pagada");

        txtBuscar.textProperty().addListener((obs, old, n) -> buscar(n));

        tblCuentas.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, nuevo) -> {
                    if (nuevo != null) mostrarDetalle(nuevo);
                });

        panelDetalle.setVisible(false);
        cargarCuentas();
    }

    private void cargarCuentas() {
        todasLasCuentas.clear();
        try (Connection con = ConexionDB.getConexionSQLite();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM cuentas ORDER BY fecha_emision DESC")) {
            while (rs.next()) {
                todasLasCuentas.add(new Cuenta(
                        rs.getString("numero_cuenta"),
                        rs.getString("funcionario_receptor"),
                        rs.getString("nombre_entidad"),
                        LocalDate.parse(rs.getString("fecha_emision")),
                        rs.getString("valor"),
                        rs.getString("estado")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        tblCuentas.setItems(todasLasCuentas);
        lblTotal.setText("Total: " + todasLasCuentas.size() + " cuentas");
    }

    private void mostrarDetalle(Cuenta c) {
        cuentaSeleccionada = c;
        panelDetalle.setVisible(true);
        lblNumeroCuenta.setText(c.getNumero());
        lblContratista.setText(c.getFuncionario());
        lblEntidad.setText(c.getEntidad());
        lblFecha.setText(c.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblValor.setText("$ " + c.getValor());
        cmbNuevoEstado.setValue(c.getEstado());
        cargarComentarios(c.getNumero());
    }

    private void cargarComentarios(String numeroCuenta) {
        ObservableList<String> comentarios = FXCollections.observableArrayList();
        String sql = """
            SELECT revisor, comentario, fecha, tipo
            FROM comentarios_cuenta
            WHERE numero_cuenta = ?
            ORDER BY fecha DESC
        """;
        try (Connection con = ConexionDB.getConexionSQLite();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, numeroCuenta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                comentarios.add(
                        "[" + rs.getString("tipo").toUpperCase() + "] " +
                                rs.getString("fecha") + "\n" +
                                rs.getString("revisor") + ": " +
                                rs.getString("comentario"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        lstComentarios.setItems(comentarios);
    }

    @FXML
    private void aplicarCambios() {
        if (cuentaSeleccionada == null) return;
        String nuevoEstado = cmbNuevoEstado.getValue();
        String comentario  = txtComentario.getText().trim();

        if (comentario.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                    "Agrega un comentario antes de aplicar cambios.").showAndWait();
            return;
        }

        String estadoAnterior = cuentaSeleccionada.getEstado();
        String revisor = SesionActual.get() != null ?
                SesionActual.get().getNombreUsuario() : "revisor";
        String fecha = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (Connection con = ConexionDB.getConexionSQLite()) {
            // Actualiza estado
            PreparedStatement ps1 = con.prepareStatement(
                    "UPDATE cuentas SET estado=? WHERE numero_cuenta=?");
            ps1.setString(1, nuevoEstado);
            ps1.setString(2, cuentaSeleccionada.getNumero());
            ps1.executeUpdate();

            // Guarda comentario
            PreparedStatement ps2 = con.prepareStatement("""
                INSERT INTO comentarios_cuenta
                (numero_cuenta, revisor, comentario, fecha, tipo)
                VALUES (?, ?, ?, ?, ?)
            """);
            ps2.setString(1, cuentaSeleccionada.getNumero());
            ps2.setString(2, revisor);
            ps2.setString(3, comentario);
            ps2.setString(4, fecha);
            ps2.setString(5, nuevoEstado.equals(estadoAnterior) ?
                    "correccion" : "cambio_estado");
            ps2.executeUpdate();

            // Historial MongoDB
            HistorialService.registrarCambio(
                    cuentaSeleccionada.getNumero(),
                    "REVISION",
                    "Estado: " + estadoAnterior + " → " + nuevoEstado +
                            " | Comentario: " + comentario,
                    revisor);

            // Notificación
            if (!estadoAnterior.equals(nuevoEstado)) {
                NotificacionService.getInstancia().agregar(
                        "Cuenta " + cuentaSeleccionada.getNumero() +
                                " revisada: " + estadoAnterior + " → " + nuevoEstado);
            }

            txtComentario.clear();
            cargarCuentas();
            cargarComentarios(cuentaSeleccionada.getNumero());

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("Cambios aplicados correctamente.");
            a.showAndWait();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Error: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void filtrar() {
        String estado = cmbFiltroEstado.getValue();
        ObservableList<Cuenta> filtradas = FXCollections.observableArrayList();
        for (Cuenta c : todasLasCuentas) {
            if (estado.equals("Todos") || c.getEstado().equals(estado))
                filtradas.add(c);
        }
        tblCuentas.setItems(filtradas);
        lblTotal.setText("Total: " + filtradas.size() + " cuentas");
    }

    private void buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            tblCuentas.setItems(todasLasCuentas);
            return;
        }
        String lower = texto.toLowerCase();
        ObservableList<Cuenta> filtradas = FXCollections.observableArrayList();
        for (Cuenta c : todasLasCuentas) {
            if (c.getNumero().toLowerCase().contains(lower) ||
                    c.getFuncionario().toLowerCase().contains(lower) ||
                    c.getEntidad().toLowerCase().contains(lower))
                filtradas.add(c);
        }
        tblCuentas.setItems(filtradas);
    }

    @FXML private void actualizar() { cargarCuentas(); }
}