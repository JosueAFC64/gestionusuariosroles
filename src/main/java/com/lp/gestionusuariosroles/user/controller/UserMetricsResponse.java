package com.lp.gestionusuariosroles.user.controller;

public record UserMetricsResponse(
        long activeUsers,
        long inactiveUsers
) {
}
