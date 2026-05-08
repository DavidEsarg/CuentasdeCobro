package com.example.cuentasdecobro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionDB {

    private static final boolean USAR_MYSQL = false;

    private static final String MYSQL_URL  = "jdbc:mysql://172.30.16.36/sistema_cuentas_db";
    private static final String MYSQL_USER = "adespindola16";
    private static final String MYSQL_PASS = "67001316";

    private static final String SQLITE_URL = "jdbc:sqlite:cuentasdecobro.db";

    public static Connection getConexion() {
        if (USAR_MYSQL) {
            return conectarMySQL();
        } else {
            return conectarSQLite();
        }
    }

    public static Connection getConexionMySQL() {
        return conectarMySQL();
    }

    public static Connection getConexionSQLite() {
        return conectarSQLite();
    }

    private static Connection conectarMySQL() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
            System.out.println("Conexion exitosa a MySQL");
            return con;
        } catch (ClassNotFoundException e) {
            System.out.println("Error: Driver MySQL no encontrado");
        } catch (SQLException e) {
            System.out.println("Error MySQL: " + e.getMessage());
        }
        return null;
    }

    private static Connection conectarSQLite() {
        try {
            Connection con = DriverManager.getConnection(SQLITE_URL);
            System.out.println("Conexion exitosa a SQLite");
            crearTablasSQLite(con);
            return con;
        } catch (SQLException e) {
            System.out.println("Error SQLite: " + e.getMessage());
        }
        return null;
    }

    private static void crearTablasSQLite(Connection con) throws SQLException {
        Statement st = con.createStatement();
        st.execute("""
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre_usuario TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                nombres TEXT NOT NULL,
                apellidos TEXT NOT NULL,
                cedula TEXT NOT NULL
            )
        """);
        st.execute("""
            CREATE TABLE IF NOT EXISTS cuentas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numero_cuenta TEXT NOT NULL,
                funcionario_receptor TEXT NOT NULL,
                nombre_entidad TEXT NOT NULL,
                fecha_emision TEXT NOT NULL,
                valor TEXT NOT NULL,
                estado TEXT NOT NULL
            )
        """);
        st.execute("""
            CREATE TABLE IF NOT EXISTS informe_actividades (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numero_contrato TEXT NOT NULL,
                obligacion TEXT NOT NULL,
                actividad_realizada TEXT NOT NULL,
                fecha TEXT NOT NULL
            )
        """);
        st.execute("""
            CREATE TABLE IF NOT EXISTS evidencias (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numero_contrato TEXT NOT NULL,
                nombre_archivo TEXT NOT NULL,
                ruta_archivo TEXT NOT NULL,
                tipo TEXT NOT NULL,
                fecha TEXT NOT NULL
            )
        """);
        System.out.println("Tablas SQLite verificadas");
    }
}