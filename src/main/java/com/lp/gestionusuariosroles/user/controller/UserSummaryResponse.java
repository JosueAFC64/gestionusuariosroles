package com.lp.gestionusuariosroles.user.controller;

import java.util.List;

// Formato en el que se devolverán los datos de todos los usuarios (para listas)
public record UserSummaryResponse(
        List<UserSummary> content,
        int totalPages,
        int totalElements,
        int page,
        int size
) {
    public record UserSummary(
            Long id,
            String nombres,
            String apellidos,
            String email,
            String rol, // EGRESADO, ADMINISTRADOR, SUPERVISOR
            Boolean estado
    ){}
}
