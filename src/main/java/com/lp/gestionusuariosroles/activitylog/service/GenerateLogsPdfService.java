package com.lp.gestionusuariosroles.activitylog.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.lp.gestionusuariosroles.activitylog.repository.ActivityLog;
import com.lp.gestionusuariosroles.activitylog.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateLogsPdfService {

    private final ActivityLogRepository repository;

    /**
     * Genera un PDF de la lista de todos los logs (actividades realizadas por supervisores, administradores)
     *
     * @return el PDF en bytes para ser descargado en el navegador del usuario
     */
    public byte[] generateLogsPdf() {
        try {
            // Crear el documento PDF
            Document document = new Document(PageSize.A4.rotate());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            // Abrir el documento
            document.open();

            // Agregar título
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Lista de Logs", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Crear la tabla
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);

            // Definir los anchos de las columnas
            float[] columnWidths = {2f, 2f, 2f, 2f};
            table.setWidths(columnWidths);

            // Agregar encabezados
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            String[] headers = {"Usuario", "Acción", "Descripción", "Fecha/Hora"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Obtener todos los usuarios
            List<ActivityLog> logs = repository.findAll();

            // Agregar datos de usuarios
            Font dataFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            for (ActivityLog log : logs) {
                table.addCell(new Phrase(log.getUsername(), dataFont));
                table.addCell(new Phrase(log.getAction(), dataFont));
                table.addCell(new Phrase(log.getDescription(), dataFont));
                table.addCell(new Phrase(String.valueOf(log.getActionDateTime()), dataFont));
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage());
        }
    }

}
