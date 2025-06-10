package com.lp.gestionusuariosroles.activitylog.contoller;

import com.lp.gestionusuariosroles.activitylog.service.ActivityLogService;
import com.lp.gestionusuariosroles.activitylog.service.GenerateLogsExcelService;
import com.lp.gestionusuariosroles.activitylog.service.GenerateLogsPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService service;
    private final GenerateLogsPdfService pdfService;
    private final GenerateLogsExcelService excelService;

    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SUPERVISOR')")
    @GetMapping
    public ResponseEntity<ActivityLogDto> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ){
        return ResponseEntity.ok(service.getAllLogs(page, size));
    }

    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SUPERVISOR')")
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(){
        byte[] pdfBytes = pdfService.generateLogsPdf();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Lista_Logs.pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SUPERVISOR')")
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportUsersExcel() {
        byte[] excelBytes = excelService.generateLogsExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "Lista_Logs.xlsx");

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

}
