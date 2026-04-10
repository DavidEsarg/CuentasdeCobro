package com.example.cuentasdecobro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final String URL = "jdbc:mysql://172.30.16.76/sistema_cuentas_db";
    private static final String USER = "adespindola16";
    private static final String PASS = "67001316";

    public static Connection getConexion() {
        Connection conexion = null;
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");


            conexion = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("✅ ¡Conexión exitosa a la base de datos!");

        } catch (ClassNotFoundException e) {
            System.out.println("❌ Error: No se encontró el Driver (Revisa el pom.xml)");
        } catch (SQLException e) {
            System.out.println("❌ Error: No se pudo conectar. Revisa usuario, clave o que el servidor esté encendido.");
            System.out.println("Mensaje: " + e.getMessage());
        }
        return conexion;
    }
}