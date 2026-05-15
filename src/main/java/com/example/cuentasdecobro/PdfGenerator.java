package com.example.cuentasdecobro;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.scene.control.Alert;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PdfGenerator {

    public static void generarCuentaCobro(Cuenta cuenta, Usuario usuario) {
        String ruta = "cuenta_cobro_" + cuenta.getNumero() + ".pdf";
        try {
            Document doc = new Document(PageSize.A4, 60, 60, 80, 60);
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(ruta));
            doc.open();

            Font fTitulo  = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.WHITE);
            Font fSubtit  = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(30, 60, 100));
            Font fNormal  = new Font(Font.FontFamily.HELVETICA, 10);
            Font fBold    = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            Font fPequeno = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);

            // ENCABEZADO
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            PdfPCell celdaHeader = new PdfPCell(new Phrase("CUENTA DE COBRO", fTitulo));
            celdaHeader.setBackgroundColor(new BaseColor(26, 58, 92));
            celdaHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            celdaHeader.setPadding(12);
            celdaHeader.setBorder(Rectangle.NO_BORDER);
            header.addCell(celdaHeader);
            doc.add(header);
            doc.add(new Paragraph(" "));

            Paragraph subtitulo = new Paragraph("República de Colombia — MiCCobro", fSubtit);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(subtitulo);
            doc.add(new Paragraph(" "));

            // DATOS DE LA CUENTA
            PdfPTable tablaDatos = new PdfPTable(2);
            tablaDatos.setWidthPercentage(100);
            tablaDatos.setWidths(new float[]{1, 2});
            tablaDatos.setSpacingBefore(10);

            agregarFila(tablaDatos, "N° Cuenta:", cuenta.getNumero(), fBold, fNormal);
            agregarFila(tablaDatos, "Fecha de emisión:",
                    cuenta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fBold, fNormal);
            agregarFila(tablaDatos, "Entidad:", cuenta.getEntidad(), fBold, fNormal);
            agregarFila(tablaDatos, "Funcionario receptor:", cuenta.getFuncionario(), fBold, fNormal);
            agregarFila(tablaDatos, "Estado:", cuenta.getEstado(), fBold, fNormal);

            PdfPCell celdaLabelValor = new PdfPCell(new Phrase("Valor total:", fBold));
            celdaLabelValor.setBackgroundColor(new BaseColor(240, 240, 240));
            celdaLabelValor.setPadding(6);
            PdfPCell celdaValor = new PdfPCell(new Phrase("$ " + cuenta.getValor(),
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(26, 58, 92))));
            celdaValor.setBackgroundColor(new BaseColor(240, 240, 240));
            celdaValor.setPadding(6);
            tablaDatos.addCell(celdaLabelValor);
            tablaDatos.addCell(celdaValor);
            doc.add(tablaDatos);
            doc.add(new Paragraph(" "));

            // DATOS DEL CONTRATISTA
            if (usuario != null) {
                Paragraph tituloContratista = new Paragraph("DATOS DEL CONTRATISTA", fSubtit);
                tituloContratista.setSpacingBefore(10);
                doc.add(tituloContratista);
                doc.add(Chunk.NEWLINE);
                doc.add(new Paragraph(" "));

                PdfPTable tablaContratista = new PdfPTable(2);
                tablaContratista.setWidthPercentage(100);
                tablaContratista.setWidths(new float[]{1, 2});
                agregarFila(tablaContratista, "Nombre completo:",
                        usuario.getNombres() + " " + usuario.getApellidos(), fBold, fNormal);
                agregarFila(tablaContratista, "Cédula:", usuario.getCedula(), fBold, fNormal);
                doc.add(tablaContratista);
                doc.add(new Paragraph(" "));
            }

            // FIRMAS
            doc.add(new Paragraph(" "));
            PdfPTable tablaFirmas = new PdfPTable(2);
            tablaFirmas.setWidthPercentage(80);
            tablaFirmas.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell firmaContratista = new PdfPCell();
            firmaContratista.setMinimumHeight(60);
            firmaContratista.setBorderWidthTop(1);
            firmaContratista.setBorderWidthBottom(0);
            firmaContratista.setBorderWidthLeft(0);
            firmaContratista.setBorderWidthRight(0);
            firmaContratista.setHorizontalAlignment(Element.ALIGN_CENTER);
            firmaContratista.addElement(new Phrase(
                    usuario != null ? usuario.getNombres() + " " + usuario.getApellidos()
                            : "_______________", fBold));
            firmaContratista.addElement(new Phrase("Contratista", fPequeno));

            PdfPCell firmaEntidad = new PdfPCell();
            firmaEntidad.setMinimumHeight(60);
            firmaEntidad.setBorderWidthTop(1);
            firmaEntidad.setBorderWidthBottom(0);
            firmaEntidad.setBorderWidthLeft(0);
            firmaEntidad.setBorderWidthRight(0);
            firmaEntidad.setHorizontalAlignment(Element.ALIGN_CENTER);
            firmaEntidad.addElement(new Phrase("_______________", fBold));
            firmaEntidad.addElement(new Phrase("Supervisor / Entidad", fPequeno));

            tablaFirmas.addCell(firmaContratista);
            tablaFirmas.addCell(firmaEntidad);
            doc.add(tablaFirmas);

            // FIRMA DIGITAL
            agregarFirmaDigital(doc, usuario, fBold, fPequeno);

            // PIE DE PAGINA
            doc.add(new Paragraph(" "));
            Paragraph pie = new Paragraph(
                    "Documento generado el " + LocalDate.now()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                            " — MiCCobro Sistema de Cuentas de Cobro", fPequeno);
            pie.setAlignment(Element.ALIGN_CENTER);
            doc.add(pie);

            doc.close();

            javafx.application.Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("PDF generado");
                a.setContentText("PDF guardado como: " + ruta);
                a.showAndWait();
            });

        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText("Error al generar PDF: " + e.getMessage());
                a.showAndWait();
            });
        }
    }

    private static void agregarFirmaDigital(Document doc, Usuario usuario,
                                            Font fBold, Font fPequeno) throws Exception {
        doc.add(new Paragraph(" "));
        PdfPTable tablaFirma = new PdfPTable(1);
        tablaFirma.setWidthPercentage(60);
        tablaFirma.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell celda = new PdfPCell();
        celda.setBackgroundColor(new BaseColor(245, 248, 255));
        celda.setBorderColor(new BaseColor(26, 58, 92));
        celda.setPadding(10);

        String nombreFirmante = usuario != null
                ? usuario.getNombres() + " " + usuario.getApellidos() : "Sin firma";
        String cedula = usuario != null ? usuario.getCedula() : "";
        String fechaHora = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        String hash = Integer.toHexString(
                (nombreFirmante + cedula + fechaHora).hashCode()).toUpperCase();

        celda.addElement(new Phrase("✦ FIRMA DIGITAL", fBold));
        celda.addElement(new Phrase("Firmado por: " + nombreFirmante, fPequeno));
        celda.addElement(new Phrase("Cédula: " + cedula, fPequeno));
        celda.addElement(new Phrase("Fecha y hora: " + fechaHora, fPequeno));
        celda.addElement(new Phrase("Código: " + hash, fPequeno));

        tablaFirma.addCell(celda);
        doc.add(tablaFirma);
    }

    private static void agregarFila(PdfPTable tabla, String label, String valor,
                                    Font fBold, Font fNormal) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, fBold));
        c1.setPadding(5);
        c1.setBackgroundColor(new BaseColor(245, 245, 245));
        PdfPCell c2 = new PdfPCell(new Phrase(valor != null ? valor : "", fNormal));
        c2.setPadding(5);
        tabla.addCell(c1);
        tabla.addCell(c2);
    }
}