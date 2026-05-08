package com.example.cuentasdecobro;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para persistir en MongoDB:
 *   - Colección "informe_actividades"
 *   - Colección "evidencias"
 *
 * Estructura de un documento informe_actividades:
 * {
 *   numero_contrato: "...",
 *   fecha: "2025-05-08",
 *   obligaciones: [
 *     { obligacion: "...", actividad_realizada: "..." },
 *     ...
 *   ]
 * }
 *
 * Estructura de un documento evidencias:
 * {
 *   numero_contrato: "...",
 *   fecha: "2025-05-08",
 *   archivos: [
 *     { nombre: "foto.jpg", tipo: "jpg", tamanio_bytes: 12345, datos: <Binary> },
 *     ...
 *   ]
 * }
 */
public class MongoService {

    private static final String COL_INFORMES   = "informe_actividades";
    private static final String COL_EVIDENCIAS = "evidencias";

    // ══════════════════════════════════════════════════════════════════════════
    //  INFORME DE ACTIVIDADES
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Guarda el informe de actividades completo en un único documento Mongo.
     *
     * @param numeroContrato número del contrato
     * @param obligaciones   lista de textos de obligaciones (del SECOP)
     * @param actividades    lista de actividades realizadas (mismo índice que obligaciones)
     */
    public static void guardarInforme(String numeroContrato,
                                      List<String> obligaciones,
                                      List<String> actividades) {
        try {
            MongoDatabase db = ConexionDB.getConexionMongo();
            if (db == null) {
                System.out.println("MongoDB no disponible – informe no guardado en Mongo.");
                return;
            }

            MongoCollection<Document> col = db.getCollection(COL_INFORMES);

            List<Document> items = new ArrayList<>();
            for (int i = 0; i < obligaciones.size(); i++) {
                String actividad = (i < actividades.size()) ? actividades.get(i) : "";
                items.add(new Document("obligacion", obligaciones.get(i))
                        .append("actividad_realizada", actividad));
            }

            Document doc = new Document("numero_contrato", numeroContrato)
                    .append("fecha", LocalDate.now().toString())
                    .append("obligaciones", items);

            col.insertOne(doc);
            System.out.println("Informe guardado en MongoDB. ID: " + doc.getObjectId("_id"));

        } catch (Exception e) {
            System.out.println("Error al guardar informe en MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  EVIDENCIAS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Guarda las evidencias en MongoDB.
     * Los archivos se almacenan como binario (byte[]) dentro del documento.
     * Si un archivo pesa más de 15 MB se omite (límite de documento BSON = 16 MB).
     *
     * @param numeroContrato número del contrato al que pertenecen las evidencias
     * @param archivos       lista de File seleccionados por el usuario
     */
    public static void guardarEvidencias(String numeroContrato, List<File> archivos) {
        if (archivos == null || archivos.isEmpty()) return;

        try {
            MongoDatabase db = ConexionDB.getConexionMongo();
            if (db == null) {
                System.out.println("MongoDB no disponible – evidencias no guardadas en Mongo.");
                return;
            }

            MongoCollection<Document> col = db.getCollection(COL_EVIDENCIAS);

            List<Document> items = new ArrayList<>();
            for (File f : archivos) {
                if (!f.exists()) continue;

                // Omitir archivos mayores a 15 MB para no superar el límite BSON
                if (f.length() > 15 * 1024 * 1024) {
                    System.out.println("Archivo omitido (>15 MB): " + f.getName());
                    continue;
                }

                byte[] bytes = leerArchivo(f);
                if (bytes == null) continue;

                String extension = obtenerExtension(f.getName());

                items.add(new Document("nombre",        f.getName())
                        .append("tipo",          extension)
                        .append("tamanio_bytes", f.length())
                        .append("ruta_original", f.getAbsolutePath())
                        .append("datos",         bytes));   // almacenado como BsonBinary
            }

            if (items.isEmpty()) {
                System.out.println("No se procesó ningún archivo de evidencia.");
                return;
            }

            Document doc = new Document("numero_contrato", numeroContrato)
                    .append("fecha",    LocalDate.now().toString())
                    .append("archivos", items);

            col.insertOne(doc);
            System.out.println("Evidencias guardadas en MongoDB. ID: " + doc.getObjectId("_id")
                    + " | Archivos: " + items.size());

        } catch (Exception e) {
            System.out.println("Error al guardar evidencias en MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  INICIALIZACIÓN DE COLECCIONES (verifica / crea índices)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Crea los índices recomendados en MongoDB.
     * Llama a este método una sola vez al iniciar la app.
     */
    public static void inicializarColecciones() {
        try {
            MongoDatabase db = ConexionDB.getConexionMongo();
            if (db == null) return;

            // Índice en numero_contrato para búsquedas rápidas
            db.getCollection(COL_INFORMES).createIndex(
                    new Document("numero_contrato", 1));
            db.getCollection(COL_EVIDENCIAS).createIndex(
                    new Document("numero_contrato", 1));

            System.out.println("Colecciones MongoDB inicializadas: "
                    + COL_INFORMES + ", " + COL_EVIDENCIAS);
        } catch (Exception e) {
            System.out.println("Error al inicializar colecciones MongoDB: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UTILIDADES PRIVADAS
    // ══════════════════════════════════════════════════════════════════════════

    private static byte[] leerArchivo(File f) {
        try (FileInputStream fis = new FileInputStream(f)) {
            return fis.readAllBytes();
        } catch (Exception e) {
            System.out.println("No se pudo leer el archivo: " + f.getName());
            return null;
        }
    }

    private static String obtenerExtension(String nombre) {
        int idx = nombre.lastIndexOf('.');
        return (idx >= 0 && idx < nombre.length() - 1)
                ? nombre.substring(idx + 1).toLowerCase()
                : "bin";
    }
}