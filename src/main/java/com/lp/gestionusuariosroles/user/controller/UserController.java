package com.lp.gestionusuariosroles.user.controller;

import com.lp.gestionusuariosroles.activitylog.service.LogActivity;
import com.lp.gestionusuariosroles.user.service.GenerateUsersExcelService;
import com.lp.gestionusuariosroles.user.service.GenerateUsersPdfService;
import com.lp.gestionusuariosroles.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final GenerateUsersPdfService pdfService;
    private final GenerateUsersExcelService excelService;

    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR','SUPERVISOR')")
    @GetMapping
    public ResponseEntity<UserSummaryResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size
    ) {
        return ResponseEntity.ok(service.getAllUsers(page, size));
    }

    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR','SUPERVISOR')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDataResponse> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(service.getUserById(id));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/session/user-data")
    public ResponseEntity<UserDataResponse> getUserInSessionData(HttpServletRequest request) {
        return ResponseEntity.ok(service.getUserInSessionData(request));
    }

    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SUPERVISOR')")
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportUsersPdf() {
        byte[] pdfBytes = pdfService.generateUsersPdf();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Lista_Usuarios.pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'SUPERVISOR')")
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportUsersExcel() {
        byte[] excelBytes = excelService.generateUsersExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "Lista_Usuarios.xlsx");

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    @LogActivity(action = "USER_UPDATED", description = "Actualizó la información de un usuario")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR','SUPERVISOR')")
    @PatchMapping("/{id}")
    public ResponseEntity<UserDataResponse> updateUser(@PathVariable Long id, @RequestBody UserDto userDto){
        return ResponseEntity.ok(service.updateUser(id, userDto));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/session/{id}")
    public ResponseEntity<UserDataResponse> updateUserInSession(@PathVariable Long id, @RequestBody UserDto userDto){
        return ResponseEntity.ok(service.updateUser(id, userDto));
    }

    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR','SUPERVISOR')")
    @GetMapping("/reportes/rol-distribution")
    public ResponseEntity<RolDistributionResponse> getRolDistribution() {
        return ResponseEntity.ok(service.getRolDistribution());
    }

    @LogActivity(action = "CHANGE_USER_STATE", description = "Cambio el estado de un usuario")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR','SUPERVISOR')")
    @PatchMapping("/estado/{id}/{nuevoEstado}")
    public ResponseEntity<Void> changeEstado(@PathVariable Long id, @PathVariable Boolean nuevoEstado){
        service.changeEstado(id, nuevoEstado);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/password")
    public ResponseEntity<UpdatePasswordResponse> changePassword(HttpServletRequest request, @RequestBody UpdatePasswordRequest passwordRequest){
        return ResponseEntity.ok(service.updatePassword(request, passwordRequest));
    }

    @LogActivity(action = "USER_DELETED", description = "Eliminó un usuario")
    @PreAuthorize("hasAnyAuthority('ADMINISTRADOR','SUPERVISOR')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response){
        service.deleteUser(id, request, response);
        return ResponseEntity.noContent().build();
    }


}
