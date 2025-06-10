package com.lp.gestionusuariosroles.auth.controller;

public record NewPasswordRequest(
        String newPassword,
        String confirmNewPassword
) {
}
