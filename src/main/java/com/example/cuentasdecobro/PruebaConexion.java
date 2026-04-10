package com.example.cuentasdecobro;

import java.sql.Connection;

public class PruebaConexion {
    public static void main(String[] args) {
        Connection c = ConexionDB.getConexion();
        if (c != null) {
            System.out.println("🚀 ¡Felicidades! Tu código ya tiene acceso a MySQL.");
        } else {
            System.out.println("💡 Revisa que el servicio de MySQL esté encendido y la clave sea correcta.");
        }
    }
}