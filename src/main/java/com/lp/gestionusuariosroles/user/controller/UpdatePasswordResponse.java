package com.lp.gestionusuariosroles.user.controller;

// Formato en el que se devolverá el resultado del proceso de cambiar la contraseña
public record UpdatePasswordResponse(
        String message,
        Boolean status
) {
}
