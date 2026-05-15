package com.example.cuentasdecobro;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

public class ConexionDB {

    private static final boolean USAR_MYSQL = false;
    private static final String MYSQL_URL   = "jdbc:mysql://172.30.16.36/sistema_cuentas_db";
    private static final String MYSQL_USER  = "adespindola16";
    private static final String MYSQL_PASS  = "67001316";
    private static final String SQLITE_URL  = "jdbc:sqlite:cuentasdecobro.db";
    private static final String MONGO_URI   = "mongodb://172.30.16.165:27017";
    private static final String MONGO_DB    = "sistema_cuentas_db";
    private static MongoClient mongoClientInstance = null;

    public static Connection getConexion() {
        return USAR_MYSQL ? conectarMySQL() : conectarSQLite();
    }
    public static Connection getConexionMySQL()  { return conectarMySQL(); }
    public static Connection getConexionSQLite() { return conectarSQLite(); }

    public static MongoDatabase getConexionMongo() {
        if (mongoClientInstance == null) {
            try {
                MongoClientSettings settings = MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(MONGO_URI))
                        .applyToClusterSettings(b -> b.serverSelectionTimeout(3, TimeUnit.SECONDS))
                        .applyToSocketSettings(b -> b
                                .connectTimeout(3, TimeUnit.SECONDS)
                                .readTimeout(3, TimeUnit.SECONDS))
                        .build();
                mongoClientInstance = MongoClients.create(settings);
            } catch (Exception e) {
                System.out.println("Error MongoDB: " + e.getMessage());
                return null;
            }
        }
        return mongoClientInstance.getDatabase(MONGO_DB);
    }

    public static void cerrarMongo() {
        if (mongoClientInstance != null) {
            mongoClientInstance.close();
            mongoClientInstance = null;
        }
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
                cedula TEXT NOT NULL,
                rol TEXT NOT NULL DEFAULT 'contratista'
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
        st.execute("""
            CREATE TABLE IF NOT EXISTS beneficiarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                cedula TEXT UNIQUE NOT NULL,
                banco TEXT NOT NULL,
                tipo_cuenta TEXT NOT NULL,
                numero_cuenta TEXT NOT NULL,
                email TEXT,
                telefono TEXT
            )
        """);
        st.execute("""
            CREATE TABLE IF NOT EXISTS contador_cuentas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                ultimo_numero INTEGER NOT NULL DEFAULT 0
            )
        """);
        st.execute("""
            CREATE TABLE IF NOT EXISTS comentarios_cuenta (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numero_cuenta TEXT NOT NULL,
                revisor TEXT NOT NULL,
                comentario TEXT NOT NULL,
                fecha TEXT NOT NULL,
                tipo TEXT NOT NULL DEFAULT 'correccion'
            )
        """);
        st.execute("INSERT OR IGNORE INTO contador_cuentas (id, ultimo_numero) VALUES (1, 0)");
    }
}