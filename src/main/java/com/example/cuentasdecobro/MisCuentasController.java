package com.example.cuentasdecobro;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class MisCuentasController {

    @FXML private TableView<Cuenta> tblMisCuentas;
    @FXML private TableColumn<Cuenta, String> colNumero;
    @FXML private TableColumn<Cuenta, String> colEntidad;
    @FXML private TableColumn<Cuenta, String> colFecha;
    @FXML private TableColumn<Cuenta, String> colValor;
    @FXML private TableColumn<Cuenta, String> colEstado;

    @FXML
    public void initialize() {
        colNumero.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getNumero()));
        colEntidad.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getEntidad()));
        colFecha.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getFecha().toString()));
        colValor.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getValor()));
        colEstado.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getEstado()));

        tblMisCuentas.setItems(
                CuentaService.getInstancia().getCuentasObservables());

        System.out.println("=== Cuentas en ArrayList ===");
        for (Cuenta c : CuentaService.getInstancia().getCuentas()) {
            System.out.println(
                    "Número: " + c.getNumero() +
                            " | Funcionario: " + c.getFuncionario() +
                            " | Entidad: " + c.getEntidad() +
                            " | Fecha: " + c.getFecha() +
                            " | Valor: " + c.getValor() +
                            " | Estado: " + c.getEstado()
            );
        }
        System.out.println("Total: " +
                CuentaService.getInstancia().getCuentas().size());
        System.out.println("===========================");
    }
}