package com.lp.gestionusuariosroles.auth.service;

import com.lp.gestionusuariosroles.exceptions.InvalidPasswordException;
import com.lp.gestionusuariosroles.exceptions.InvalidTokenException;
import com.lp.gestionusuariosroles.auth.controller.NewPasswordRequest;
import com.lp.gestionusuariosroles.exceptions.PasswordMismatchException;
import com.lp.gestionusuariosroles.user.repository.User;
import com.lp.gestionusuariosroles.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import okhttp3.*;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResendService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private static final String URL_LOGIN = "http://localhost:5173/login";

    /**
     * Envía un email al correo especificado brindando instrucciones para reestablecer la contraseña
     *
     * @param toEmail El email al que se le enviará el correo
     * @param resetLink URL que llevará al usuario a reestablecer su contraseña
     * @throws IOException Por si ocurre algún error durante el envío
     */
    public void sendPasswordResetEmail(String toEmail, String resetLink) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String htmlContent = String.format(
                "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden;\">" +
                        "  <div style=\"background-color: #1a1a1a; padding: 20px; text-align: center;\">" +
                        "    <h1 style=\"color: white; margin: 0;\">Restablecimiento de contraseña</h1>" +
                        "  </div>" +
                        "  <div style=\"padding: 30px; background-color: #f9fafb;\">" +
                        "    <p>Hemos recibido una solicitud para restablecer tu contraseña. Completa esta acción haciendo clic en el botón a continuación:</p>" +
                        "    <div style=\"text-align: center; margin: 25px 0;\">" +
                        "      <a href=\"%s\" style=\"background-color: #1a1a1a; color: white; padding: 12px 20px; text-decoration: none; border-radius: 4px; display: inline-block;\">Restablecer contraseña</a>" +
                        "    </div>" +
                        "    <p style=\"font-size: 14px; color: #6b7280;\">Si no realizaste esta solicitud, ignora este email. Tu cuenta permanece segura y tu contraseña actual no se cambiará.</p>" +
                        "  </div>" +
                        "</div>",
                resetLink
        );

        String jsonBody = String.format(
                "{\"from\": \"%s\", \"to\": [\"%s\"], \"subject\": \"Instrucciones para restablecer tu contraseña\", " +
                        "\"html\": \"%s\"}",
                fromEmail,
                toEmail,
                htmlContent.replace("\"", "\\\"")  // Escapar comillas para JSON
        );

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(RESEND_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + resendApiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                assert response.body() != null;
                throw new IOException("Error al enviar el correo: " + response.body().string());
            }
        }
    }

    /**
     * Envía un correo al email especificado para informar el reestablecimiento de su contraseña
     *
     * @param toEmail Correo al que se le enviará la confirmación
     * @throws IOException Por si ocurre algún error durante el envío
     */
    private void sendConfirmNewPasswordEmail(String toEmail) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String htmlContent = String.format(
                "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden;\">" +
                "  <div style=\"background-color: #1a1a1a; padding: 20px; text-align: center;\">" +
                "    <h1 style=\"color: white; margin: 0;\">Tu contraseña ha sido cambiada exitosamente</h1>" +
                "  </div>" +
                "  <div style=\"padding: 30px; background-color: #f9fafb;\">" +
                "    <p>Inicia sesión en tu cuenta haciendo clic en el botón de abajo:</p>" +
                "    <div style=\"text-align: center; margin: 25px 0;\">" +
                "      <a href=\"%s\" style=\"background-color: #1a1a1a; color: white; padding: 12px 20px; text-decoration: none; border-radius: 4px; display: inline-block;\">Iniciar Sesión</a>" +
                "    </div>" +
                "  </div>" +
                "</div>",
                URL_LOGIN
        );

        String jsonBody = String.format(
                "{\"from\": \"%s\", \"to\": [\"%s\"], \"subject\": \"Contraseña cambiada\", " +
                        "\"html\": \"%s\"}",
                fromEmail,
                toEmail,
                htmlContent.replace("\"", "\\\"")  // Escapar comillas para JSON
        );

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(RESEND_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + resendApiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                assert response.body() != null;
                throw new IOException("Error al enviar el correo: " + response.body().string());
            }
        }
    }

    public void sendTwoFactorCodeEmail(String toEmail, String twoFactorCode) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String htmlContent = String.format(
                "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden;\">" +
                        "  <div style=\"background-color: #1a1a1a; padding: 20px; text-align: center;\">" +
                        "    <h1 style=\"color: white; margin: 0;\">Código de Autenticación de 2 Factores</h1>" +
                        "  </div>" +
                        "  <div style=\"padding: 30px; background-color: #f9fafb;\">" +
                        "    <p>Este es tú codigo de autenticación de 2 factores:</p>" +
                        "    <div style=\"text-align: center; margin: 25px 0;\">" +
                        "     <p> " + twoFactorCode + "</p> " +
                        "    </div>" +
                        "  </div>" +
                        "</div>"
        );

        String jsonBody = String.format(
                "{\"from\": \"%s\", \"to\": [\"%s\"], \"subject\": \"Código 2FA\", " +
                        "\"html\": \"%s\"}",
                fromEmail,
                toEmail,
                htmlContent.replace("\"", "\\\"")  // Escapar comillas para JSON
        );

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(RESEND_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + resendApiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                assert response.body() != null;
                throw new IOException("Error al enviar el correo: " + response.body().string());
            }
        }
    }

    /**
     * Se encarga de generar el link de reestablecimiento de contraseña y enviar el correo
     * con las instrucciones para reestablecerla
     *
     * @param email Email del usuario que olvidó su contraseña
     * @return Un mensaje de éxito o fallo de la operación
     */
    @Transactional
    public String forgotPassword(String email) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        repository.save(user);

        String resetLink = "http://localhost:5173/reset-password?token=" + resetToken;

        try{
            sendPasswordResetEmail(email, resetLink);
            return "Si el email existe, se enviará en enlace de recuperación";
        } catch (IOException e) {
            return "Error al enviar el correo: " + e.getMessage();
        }
    }

    /**
     * Se encarga de recibir la nueva contraseña y reestablecerla
     *
     * @param token Token generado aleatoriamente que sirve como identificador para la operación
     * @param request Datos necesarios para reestablecer la contraseña
     * @return Un mensaje de éxito o fallo de la operación
     */
    @Transactional
    public String resetPassword(String token, NewPasswordRequest request) {
        User user = repository.findByResetToken(token);
        if(user == null){
            throw new InvalidTokenException("Token inválido o expirado");
        }

        if (!request.newPassword().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
            throw new InvalidPasswordException("La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial");
        }
        if(!request.newPassword().equals(request.confirmNewPassword())){
            throw new PasswordMismatchException("Las contraseñas deben ser iguales");
        }
        if(passwordEncoder.matches(request.newPassword(),user.getPassword())){
            throw new InvalidPasswordException("La contraseña nueva no puede ser igual a la antigua");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setResetToken(null);
        repository.save(user);

        try{
            sendConfirmNewPasswordEmail(user.getEmail());
            return "Contraseña cambiada exitosamente";
        } catch (IOException e) {
            return "Error al enviar el correo: " + e.getMessage();
        }
    }
}
