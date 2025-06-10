package com.lp.gestionusuariosroles.activitylog.service;

import com.lp.gestionusuariosroles.activitylog.repository.ActivityLog;
import com.lp.gestionusuariosroles.activitylog.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenerateLogsExcelService {

    private final ActivityLogRepository repository;

    public byte[] generateLogsExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Lista de Logs");

            // Crear estilo para el encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Crear la fila de encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Usuario", "Acción", "Descripción", "Fecha/Hora"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Obtener todos los usuarios
            List<ActivityLog> logs = repository.findAll();

            // Llenar datos
            int rowNum = 1;
            for (ActivityLog log : logs) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(log.getUsername());
                row.createCell(1).setCellValue(log.getAction());
                row.createCell(2).setCellValue(log.getDescription());
                row.createCell(3).setCellValue(String.valueOf(log.getActionDateTime()));
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
