package com.example.cuentasdecobro;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

public class InformeController {

    @FXML private TextField txtContrato;
    @FXML private VBox vboxObligaciones;
    @FXML private Label lblEvidencias;

    private List<String> obligaciones      = new ArrayList<>();
    private List<TextArea> camposActividad = new ArrayList<>();
    private List<File> archivosEvidencia   = new ArrayList<>();

    // ══════════════════════════════════════════════════════════════════════════
    //  BUSCAR CONTRATO EN SECOP
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void buscarContrato() {
        String contrato = txtContrato.getText().trim();
        if (contrato.isEmpty()) {
            mostrarAlerta("Ingresa el número de contrato.");
            return;
        }
        vboxObligaciones.getChildren().clear();
        camposActividad.clear();
        obligaciones.clear();

        Label lblCargando = new Label("Consultando SECOP...");
        vboxObligaciones.getChildren().add(lblCargando);

        new Thread(() -> {
            List<String> resultado = SecopService.buscarObligaciones(contrato);
            javafx.application.Platform.runLater(() -> {
                vboxObligaciones.getChildren().clear();
                obligaciones.addAll(resultado);
                for (String ob : resultado) {
                    Label lbl = new Label(ob);
                    lbl.setWrapText(true);
                    lbl.setStyle("-fx-font-weight: bold; -fx-padding: 5 0 2 0;");
                    TextArea ta = new TextArea();
                    ta.setPromptText("Escribe las actividades realizadas...");
                    ta.setPrefHeight(80);
                    ta.setWrapText(true);
                    camposActividad.add(ta);
                    vboxObligaciones.getChildren().addAll(lbl, ta);
                }
            });
        }).start();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SUBIR EVIDENCIAS
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void subirEvidencia() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar evidencias");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Todos", "*.jpg","*.jpeg","*.png","*.pdf","*.docx","*.doc"),
                new FileChooser.ExtensionFilter("Imágenes", "*.jpg","*.jpeg","*.png"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Word", "*.docx","*.doc")
        );
        List<File> archivos = fc.showOpenMultipleDialog(null);
        if (archivos != null) {
            archivosEvidencia.addAll(archivos);
            StringBuilder sb = new StringBuilder("Archivos seleccionados:\n");
            for (File f : archivosEvidencia) sb.append("- ").append(f.getName()).append("\n");
            lblEvidencias.setText(sb.toString());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GUARDAR (SQLite + MongoDB) Y GENERAR PDF
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void guardarInforme() {
        if (txtContrato.getText().isEmpty() || camposActividad.isEmpty()) {
            mostrarAlerta("Primero busca un contrato y llena las actividades.");
            return;
        }

        String contrato = txtContrato.getText().trim();
        String fecha    = LocalDate.now().toString();

        // Recoger textos de actividades
        List<String> textosActividades = new ArrayList<>();
        for (TextArea ta : camposActividad) {
            textosActividades.add(ta.getText());
        }

        // ── 1. Guardar en SQLite (comportamiento original) ──────────────────
        guardarEnSQLite(contrato, fecha, textosActividades);

        // ── 2. Guardar informe en MongoDB ────────────────────────────────────
        MongoService.guardarInforme(contrato, obligaciones, textosActividades);

        // ── 3. Guardar evidencias en MongoDB ─────────────────────────────────
        MongoService.guardarEvidencias(contrato, archivosEvidencia);

        // ── 4. Generar PDF ───────────────────────────────────────────────────
        generarPDF(contrato, fecha);
    }

    private void guardarEnSQLite(String contrato, String fecha, List<String> textosActividades) {
        try (Connection con = ConexionDB.getConexionSQLite()) {

            // Informe de actividades
            String sql1 = "INSERT INTO informe_actividades "
                    + "(numero_contrato, obligacion, actividad_realizada, fecha) VALUES (?, ?, ?, ?)";
            for (int i = 0; i < obligaciones.size(); i++) {
                PreparedStatement ps = con.prepareStatement(sql1);
                ps.setString(1, contrato);
                ps.setString(2, obligaciones.get(i));
                ps.setString(3, textosActividades.get(i));
                ps.setString(4, fecha);
                ps.executeUpdate();
            }

            // Evidencias (solo metadatos en SQLite, binarios en Mongo)
            String sql2 = "INSERT INTO evidencias "
                    + "(numero_contrato, nombre_archivo, ruta_archivo, tipo, fecha) VALUES (?, ?, ?, ?, ?)";
            for (File f : archivosEvidencia) {
                PreparedStatement ps = con.prepareStatement(sql2);
                ps.setString(1, contrato);
                ps.setString(2, f.getName());
                ps.setString(3, f.getAbsolutePath());
                ps.setString(4, f.getName().substring(f.getName().lastIndexOf(".") + 1));
                ps.setString(5, fecha);
                ps.executeUpdate();
            }

            System.out.println("Informe y evidencias guardados en SQLite.");

        } catch (SQLException e) {
            mostrarAlerta("Error al guardar en SQLite: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GENERACIÓN DE PDF
    // ══════════════════════════════════════════════════════════════════════════

    private void generarPDF(String contrato, String fecha) {
        try {
            String ruta = "informe_" + contrato + "_" + fecha + ".pdf";
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();

            Font tituloFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font boldFont   = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 11);

            doc.add(new Paragraph("INFORME DE ACTIVIDADES", tituloFont));
            doc.add(new Paragraph("Contrato N°: " + contrato, boldFont));
            doc.add(new Paragraph("Fecha: " + fecha, normalFont));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("OBLIGACIONES Y ACTIVIDADES REALIZADAS", boldFont));
            doc.add(new Paragraph(" "));

            for (int i = 0; i < obligaciones.size(); i++) {
                doc.add(new Paragraph("Obligacion " + (i + 1) + ": " + obligaciones.get(i), boldFont));
                String actividad = camposActividad.get(i).getText();
                doc.add(new Paragraph("Actividad: " +
                        (actividad.isEmpty() ? "Sin actividad registrada" : actividad), normalFont));
                doc.add(new Paragraph(" "));
            }

            if (!archivosEvidencia.isEmpty()) {
                doc.add(new Paragraph("EVIDENCIAS ADJUNTAS:", boldFont));
                for (File f : archivosEvidencia)
                    doc.add(new Paragraph("- " + f.getName(), normalFont));
            }

            doc.close();
            mostrarExito("PDF generado: " + ruta
                    + "\n\nDatos también guardados en MongoDB.");

        } catch (Exception e) {
            mostrarAlerta("Error al generar PDF: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS UI
    // ══════════════════════════════════════════════════════════════════════════

    private void mostrarAlerta(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void mostrarExito(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.showAndWait();
    }
}