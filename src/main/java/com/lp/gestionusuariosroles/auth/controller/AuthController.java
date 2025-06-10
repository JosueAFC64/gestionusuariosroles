package com.lp.gestionusuariosroles.auth.controller;

import com.lp.gestionusuariosroles.activitylog.service.LogActivity;
import com.lp.gestionusuariosroles.auth.service.AuthService;
import com.lp.gestionusuariosroles.auth.service.ResendService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;
    private final ResendService resendService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateV2(@RequestBody AuthRequest request, HttpServletResponse response) throws IOException {
        return ResponseEntity.ok(service.initiateAuthentication(request, response));
    }

    @PostMapping("/login/2fa/verify")
    public ResponseEntity<Void> verify2faAndAuthenticate(@RequestParam String email, @RequestParam String code, HttpServletResponse response) {
        service.verify2faAndAuthenticate(email, code, response);
        return ResponseEntity.noContent().build();
    }

    @LogActivity(action = "USER_CREATED", description = "Registró un nuevo usuario")
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest request) {
        service.register(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email){
        return ResponseEntity.ok(resendService.forgotPassword(email));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestBody NewPasswordRequest request){
        return ResponseEntity.ok(resendService.resetPassword(token, request));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/user/2fa/change/{id}")
    public ResponseEntity<String> change2fa(@PathVariable Long id){
        return ResponseEntity.ok(service.change2fa(id));
    }
}
