package com.example.cuentasdecobro;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppLogger {

    private static final String COLECCION = "logs";

    public static void info(String usuario, String accion, String detalle) {
        new Thread(() -> guardarLog("INFO", usuario, accion, detalle)).start();
    }

    public static void error(String usuario, String accion, String detalle) {
        new Thread(() -> guardarLog("ERROR", usuario, accion, detalle)).start();
    }

    private static void guardarLog(String nivel, String usuario, String accion, String detalle) {
        try {
            MongoDatabase db = ConexionDB.getConexionMongo();
            if (db == null) {
                System.out.println("Log omitido (MongoDB no disponible): " + accion);
                return;
            }
            MongoCollection<Document> col = db.getCollection(COLECCION);
            Document log = new Document()
                    .append("nivel", nivel)
                    .append("usuario", usuario)
                    .append("accion", accion)
                    .append("detalle", detalle)
                    .append("fecha", LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            col.insertOne(log);
        } catch (Exception e) {
            System.out.println("Log omitido: " + e.getMessage());
        }
    }
}