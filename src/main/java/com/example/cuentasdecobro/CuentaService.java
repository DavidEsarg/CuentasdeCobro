package com.example.cuentasdecobro;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;

public class CuentaService {

    private static CuentaService instancia;
    private ArrayList<Cuenta> cuentas = new ArrayList<>();
    private ObservableList<Cuenta> cuentasObservables =
            FXCollections.observableArrayList(cuentas);

    private CuentaService() {}

    public static CuentaService getInstancia() {
        if (instancia == null) {
            instancia = new CuentaService();
        }
        return instancia;
    }

    public void agregarCuenta(Cuenta cuenta) {
        cuentas.add(cuenta);
        cuentasObservables.add(cuenta);
    }

    public ArrayList<Cuenta> getCuentas() {
        return cuentas;
    }

    public ObservableList<Cuenta> getCuentasObservables() {
        return cuentasObservables;
    }
}