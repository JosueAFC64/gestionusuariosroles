package com.lp.gestionusuariosroles.user.controller;

// Datos requeridos para actualizar la contraseña
public record   UpdatePasswordRequest(
        String currentPassword,
        String newPassword
) {
}
