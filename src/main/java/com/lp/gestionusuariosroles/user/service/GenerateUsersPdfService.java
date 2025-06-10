package com.lp.gestionusuariosroles.user.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.lp.gestionusuariosroles.user.repository.User;
import com.lp.gestionusuariosroles.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateUsersPdfService {

    private final UserRepository repository;

    /**
     * Genera un PDF de la lista de todos los usuarios
     *
     * @return el PDF en bytes para ser descargado en el navegador del usuario
     */
    public byte[] generateUsersPdf() {
        try {
            // Crear el documento PDF
            Document document = new Document(PageSize.A4.rotate());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            // Abrir el documento
            document.open();

            // Agregar título
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Lista de Usuarios", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Crear la tabla
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);

            // Definir los anchos de las columnas
            float[] columnWidths = {2f, 2f, 3f, 2f, 2f, 2f, 2f, 2f};
            table.setWidths(columnWidths);

            // Agregar encabezados
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            String[] headers = {"Nombres", "Apellidos", "Email", "Rol", "Estado", "DNI", "Teléfono", "Fecha Nacimiento"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Obtener todos los usuarios
            List<User> users = repository.findAll();

            // Agregar datos de usuarios
            Font dataFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            for (User user : users) {
                table.addCell(new Phrase(user.getNombres(), dataFont));
                table.addCell(new Phrase(user.getApellidos(), dataFont));
                table.addCell(new Phrase(user.getEmail(), dataFont));
                table.addCell(new Phrase(user.getRol(), dataFont));
                table.addCell(new Phrase(user.getEstado() ? "Activo" : "Inactivo", dataFont));
                table.addCell(new Phrase(user.getDni(), dataFont));
                table.addCell(new Phrase(user.getTelefono(), dataFont));
                table.addCell(new Phrase(user.getFechaNacimiento() != null ?
                        user.getFechaNacimiento().toString() : ""));
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage());
        }
    }

}
