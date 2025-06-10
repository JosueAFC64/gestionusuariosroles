package com.lp.gestionusuariosroles.user.controller;

import java.util.Map;

public record RolDistributionResponse(
        Map<String, Long> distribution
) {}