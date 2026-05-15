package com.example.cuentasdecobro;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class NotificacionService {

    private static NotificacionService instancia;
    private IntegerProperty badge = new SimpleIntegerProperty(0);
    private ObservableList<String> notificaciones = FXCollections.observableArrayList();

    private NotificacionService() {}

    public static NotificacionService getInstancia() {
        if (instancia == null) instancia = new NotificacionService();
        return instancia;
    }

    public void agregar(String mensaje) {
        notificaciones.add(0, mensaje);
        badge.set(badge.get() + 1);
    }

    public void limpiar() {
        badge.set(0);
    }

    public IntegerProperty badgeProperty() { return badge; }
    public int getBadge()                  { return badge.get(); }
    public ObservableList<String> getNotificaciones() { return notificaciones; }
}