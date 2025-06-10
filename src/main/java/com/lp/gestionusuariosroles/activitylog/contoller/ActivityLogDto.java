package com.lp.gestionusuariosroles.activitylog.contoller;

import java.util.List;

public record ActivityLogDto(
        List<LogDto> content,
        int totalPages,
        int totalElements,
        int page,
        int size
) {
}
