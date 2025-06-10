package com.lp.gestionusuariosroles.auth.controller;

public record AuthResponse(
        Boolean requires2fa,
        String message
) {
}
