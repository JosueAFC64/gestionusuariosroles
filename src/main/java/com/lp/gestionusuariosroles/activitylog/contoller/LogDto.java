package com.lp.gestionusuariosroles.activitylog.contoller;

import java.time.LocalDateTime;

public record LogDto(
        String username,
        String action,
        String description,
        LocalDateTime actionDateTime
) {
}
