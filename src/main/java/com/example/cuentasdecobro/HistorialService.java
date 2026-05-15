package com.example.cuentasdecobro;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistorialService {

    public static void registrarCambio(String numeroCuenta, String accion,
                                       String detalle, String usuario) {
        new Thread(() -> {
            try {
                MongoDatabase db = ConexionDB.getConexionMongo();
                if (db == null) return;
                MongoCollection<Document> col = db.getCollection("historial_cuentas");
                col.insertOne(new Document()
                        .append("numero_cuenta", numeroCuenta)
                        .append("accion", accion)
                        .append("detalle", detalle)
                        .append("usuario", usuario)
                        .append("fecha", LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            } catch (Exception e) {
                System.out.println("Historial no guardado: " + e.getMessage());
            }
        }).start();
    }
}