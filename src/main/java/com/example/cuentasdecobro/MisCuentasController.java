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

public class MisCuentasController {

    @FXML private TableView<Cuenta> tblMisCuentas;
    @FXML private TableColumn<Cuenta, String> colNumero;
    @FXML private TableColumn<Cuenta, String> colFuncionario;
    @FXML private TableColumn<Cuenta, String> colEntidad;
    @FXML private TableColumn<Cuenta, String> colFecha;
    @FXML private TableColumn<Cuenta, String> colValor;
    @FXML private TableColumn<Cuenta, String> colEstado;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal;

    private ObservableList<Cuenta> todasLasCuentas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNumero.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getNumero()));
        colFuncionario.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getFuncionario()));
        colEntidad.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getEntidad()));
        colFecha.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getFecha().toString()));
        colValor.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getValor()));
        colEstado.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getEstado()));

        cmbFiltroEstado.getItems().addAll("Todos","Pendiente","Aprobada","Rechazada","Pagada");
        cmbFiltroEstado.setValue("Todos");

        txtBuscar.textProperty().addListener((obs, old, nuevo) -> buscarEnTiempoReal(nuevo));
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
        tblMisCuentas.setItems(todasLasCuentas);
        lblTotal.setText("Total: " + todasLasCuentas.size() + " registros");
    }

    private void buscarEnTiempoReal(String texto) {
        if (texto == null || texto.isBlank()) {
            tblMisCuentas.setItems(todasLasCuentas);
            lblTotal.setText("Total: " + todasLasCuentas.size() + " registros");
            return;
        }
        String lower = texto.toLowerCase();
        ObservableList<Cuenta> filtradas = FXCollections.observableArrayList();
        for (Cuenta c : todasLasCuentas) {
            if (c.getNumero().toLowerCase().contains(lower) ||
                    c.getEntidad().toLowerCase().contains(lower) ||
                    c.getFuncionario().toLowerCase().contains(lower) ||
                    c.getEstado().toLowerCase().contains(lower))
                filtradas.add(c);
        }
        tblMisCuentas.setItems(filtradas);
        lblTotal.setText("Total: " + filtradas.size() + " registros");
    }

    @FXML
    private void filtrar() {
        String estado = cmbFiltroEstado.getValue();
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        ObservableList<Cuenta> filtradas = FXCollections.observableArrayList();
        for (Cuenta c : todasLasCuentas) {
            boolean pasaEstado = estado.equals("Todos") || c.getEstado().equals(estado);
            boolean pasaDesde  = desde == null || !c.getFecha().isBefore(desde);
            boolean pasaHasta  = hasta == null || !c.getFecha().isAfter(hasta);
            if (pasaEstado && pasaDesde && pasaHasta) filtradas.add(c);
        }
        tblMisCuentas.setItems(filtradas);
        lblTotal.setText("Total: " + filtradas.size() + " registros");
    }

    @FXML
    private void limpiarFiltros() {
        cmbFiltroEstado.setValue("Todos");
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        txtBuscar.clear();
        tblMisCuentas.setItems(todasLasCuentas);
        lblTotal.setText("Total: " + todasLasCuentas.size() + " registros");
    }

    @FXML
    private void editarCuenta() {
        Cuenta c = tblMisCuentas.getSelectionModel().getSelectedItem();
        if (c == null) { mostrarAlerta("Selecciona una cuenta para editar."); return; }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar cuenta " + c.getNumero());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<String> cmbEstado = new ComboBox<>();
        cmbEstado.getItems().addAll("Pendiente","Aprobada","Rechazada","Pagada");
        cmbEstado.setValue(c.getEstado());
        TextField txtValor = new TextField(c.getValor());

        VBox box = new VBox(10,
                new Label("Estado:"), cmbEstado,
                new Label("Valor:"), txtValor);
        box.setPadding(new javafx.geometry.Insets(15));
        dialog.getDialogPane().setContent(box);

        dialog.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                String estadoAnterior = c.getEstado();
                try (Connection con = ConexionDB.getConexionSQLite();
                     PreparedStatement ps = con.prepareStatement(
                             "UPDATE cuentas SET estado=?, valor=? WHERE numero_cuenta=?")) {
                    ps.setString(1, cmbEstado.getValue());
                    ps.setString(2, txtValor.getText());
                    ps.setString(3, c.getNumero());
                    ps.executeUpdate();

                    if (!estadoAnterior.equals(cmbEstado.getValue())) {
                        NotificacionService.getInstancia().agregar(
                                "Cuenta " + c.getNumero() + ": " +
                                        estadoAnterior + " → " + cmbEstado.getValue());
                    }

                    HistorialService.registrarCambio(c.getNumero(), "EDICION",
                            "Estado: " + estadoAnterior + " → " + cmbEstado.getValue(),
                            SesionActual.get() != null ?
                                    SesionActual.get().getNombreUsuario() : "sistema");
                    cargarCuentas();
                } catch (Exception e) { mostrarAlerta("Error: " + e.getMessage()); }
            }
        });
    }

    @FXML
    private void eliminarCuenta() {
        Cuenta c = tblMisCuentas.getSelectionModel().getSelectedItem();
        if (c == null) { mostrarAlerta("Selecciona una cuenta para eliminar."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la cuenta " + c.getNumero() + "?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try (Connection con = ConexionDB.getConexionSQLite();
                     PreparedStatement ps = con.prepareStatement(
                             "DELETE FROM cuentas WHERE numero_cuenta=?")) {
                    ps.setString(1, c.getNumero());
                    ps.executeUpdate();
                    HistorialService.registrarCambio(c.getNumero(), "ELIMINACION",
                            "Cuenta eliminada",
                            SesionActual.get() != null ?
                                    SesionActual.get().getNombreUsuario() : "sistema");
                    cargarCuentas();
                } catch (Exception e) { mostrarAlerta("Error: " + e.getMessage()); }
            }
        });
    }

    @FXML
    private void verComentarios() {
        Cuenta c = tblMisCuentas.getSelectionModel().getSelectedItem();
        if (c == null) { mostrarAlerta("Selecciona una cuenta."); return; }

        ObservableList<String> comentarios = FXCollections.observableArrayList();
        String sql = """
            SELECT revisor, comentario, fecha, tipo
            FROM comentarios_cuenta
            WHERE numero_cuenta = ?
            ORDER BY fecha DESC
        """;
        try (Connection con = ConexionDB.getConexionSQLite();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNumero());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                comentarios.add(
                        "[" + rs.getString("tipo").toUpperCase() + "] " +
                                rs.getString("fecha") + "\n" +
                                rs.getString("revisor") + ": " +
                                rs.getString("comentario") + "\n");
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (comentarios.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Esta cuenta no tiene revisiones aún.").showAndWait();
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Revisiones - " + c.getNumero());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        ListView<String> lista = new ListView<>(comentarios);
        lista.setPrefSize(450, 300);
        dialog.getDialogPane().setContent(lista);
        dialog.showAndWait();
    }

    @FXML
    private void generarPDFCuenta() {
        Cuenta c = tblMisCuentas.getSelectionModel().getSelectedItem();
        if (c == null) { mostrarAlerta("Selecciona una cuenta primero."); return; }
        PdfGenerator.generarCuentaCobro(c, SesionActual.get());
    }

    private void mostrarAlerta(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).showAndWait();
    }
}