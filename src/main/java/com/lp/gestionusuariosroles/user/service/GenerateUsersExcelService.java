package com.lp.gestionusuariosroles.user.service;

import org.apache.poi.ss.usermodel.Font;
import com.lp.gestionusuariosroles.user.repository.User;
import com.lp.gestionusuariosroles.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateUsersExcelService {

    private final UserRepository repository;

    public byte[] generateUsersExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Usuarios");

            // Crear estilo para el encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Crear la fila de encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Nombres", "Apellidos", "Email", "Rol", "Estado", "DNI", "Teléfono", "Fecha Nacimiento"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Obtener todos los usuarios
            List<User> users = repository.findAll();

            // Llenar datos
            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getNombres());
                row.createCell(1).setCellValue(user.getApellidos());
                row.createCell(2).setCellValue(user.getEmail());
                row.createCell(3).setCellValue(user.getRol());
                row.createCell(4).setCellValue(user.getEstado() ? "Activo" : "Inactivo");
                row.createCell(5).setCellValue(user.getDni());
                row.createCell(6).setCellValue(user.getTelefono());
                row.createCell(7).setCellValue(user.getFechaNacimiento() != null ?
                        user.getFechaNacimiento().toString() : "");
            }

            // Autoajustar el ancho de las columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Convertir el workbook a bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error al generar el Excel: " + e.getMessage());
        }
    }

}
