package com.lp.gestionusuariosroles.auth.service;

import com.lp.gestionusuariosroles.auth.controller.AuthRequest;
import com.lp.gestionusuariosroles.auth.controller.AuthResponse;
import com.lp.gestionusuariosroles.auth.controller.RegisterRequest;
import com.lp.gestionusuariosroles.auth.repository.Token;
import com.lp.gestionusuariosroles.auth.repository.TokenRepository;
import com.lp.gestionusuariosroles.user.repository.User;
import com.lp.gestionusuariosroles.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final ResendService resendService;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Inicia el proceso de autenticación
     * Si el usuario tiene 2FA activado, enviará un código de 6 dígitos a su correo
     * que se le pedirá ingresar luego para autenticarse.
     * Si el usuario no tiene 2FA activado, lo autenticará.
     *
     * @param request Datos necesarios para realizar la autenticación
     * @param response El servidor enviará una cookie con el JWT
     * @return Un mensaje por si el usuario necesita pasar la autenticación en dos pasos primero o no
     * @throws IOException Por si algo sale mal durante la operación
     */
    @Transactional
    public AuthResponse initiateAuthentication(AuthRequest request, HttpServletResponse response) throws IOException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = repository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (user.getIs2faEnabled()){
            String verificationCode = generateSecureRandomCode();
            user.setTwoFactorCode(verificationCode);
            repository.save(user);
            resendService.sendTwoFactorCodeEmail(user.getEmail(), user.getTwoFactorCode());
            return new AuthResponse(true, "Se requiere verificación 2FA, Un correo con el " +
                    "código de verificación ha sido enviado a su correo.");
        }

        final String token = jwtService.generateToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, token);

        var cookie = new jakarta.servlet.http.Cookie("USER_SESSION", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtExpiration)/1000);

        response.addCookie(cookie);

        return new AuthResponse(false, "");
    }

    /**
     * Verifica el código de autenticación de dos factores y autentica al usuario
     *
     * @param email Email del usuario a autenticar
     * @param code Código de 6 dígitos para verificar la autenticación en dos pasos
     * @param response El servidor enviará una cookie con el JWT
     */
    @Transactional
    public void verify2faAndAuthenticate(String email, String code, HttpServletResponse response) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if(!user.getTwoFactorCode().equals(code)){
            throw new BadCredentialsException("Código 2FA inválido");
        }

        final String token = jwtService.generateToken(user);

        revokeAllUserTokens(user);
        saveUserToken(user, token);

        user.setTwoFactorCode(null);
        repository.save(user);

        var cookie = new jakarta.servlet.http.Cookie("USER_SESSION", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtExpiration)/1000);

        response.addCookie(cookie);
    }

    /**
    * Registra al usuario y lo guarda en la Base de Datos
    *
    * @param request - Datos necesarios para crear un nuevo usuario
    */
    @Transactional
    public void register(RegisterRequest request){

        // Validaciones
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de registro no puede ser nula");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        if (!request.email().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Formato de email inválido");
        }
        if(repository.findByEmail(request.email()).isPresent()){
            throw new IllegalArgumentException("El email ya existe");
        }
        if (!request.password().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial");
        }
        if (request.telefono().length() != 9){
            throw new IllegalArgumentException("El telefono debe tener 9 dígitos");
        }
        if (request.dni().length() != 8){
            throw new IllegalArgumentException("El dni debe tener 8 dígitos");
        }
        if (request.fechaNacimiento() != null && request.fechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }

        repository.save(
            User.builder()
                    .nombres(request.nombres())
                    .apellidos(request.apellidos())
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .rol(request.rol())
                    .fechaNacimiento(request.fechaNacimiento())
                    .telefono(request.telefono())
                    .dni(request.dni())
                    .estado(true)
                    .build()
        );
    }

    /**
    * Guarda el token del usuario en la Base de Datos
    *
    * @param user Objeto {@link com.lp.gestionusuariosroles.user.repository.User} (solo almacena el ID del usuario)
    * @param jwtToken El JWT generado del usuario
    */
    private void saveUserToken(User user, String jwtToken){
        final Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(Token.TokenType.BEARER)
                .isExpired(false)
                .isRevoked(false)
                .build();
        tokenRepository.save(token);
    }

    /**
    * Invalida todos los token de un usuario
    *
    * @param user Objeto {@link com.lp.gestionusuariosroles.user.repository.User} del usuario a invalidar sus tokens
    * */
    private void revokeAllUserTokens(final User user){
        final List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if(!validUserTokens.isEmpty()){
            validUserTokens.forEach(token -> {
                token.setIsRevoked(true);
                token.setIsExpired(true);
            });
            tokenRepository.saveAll(validUserTokens);
        }
    }

    /**
     * Invalida los tokens de un usuario, hace uso de un servicio privado
     * De uso puntual cuando el estado de un usuario se vuelve "false" o cuando se elimina un usuario
     *
     * @param userId Id del usuario a invalidar sus tokens
     */
    @Transactional
    public void revokeAllTokensForUser(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        revokeAllUserTokens(user);
    }

    /**
     * Genera un código de 6 dígitos para la 2FA
     * @return El código de 6 dígitos parseado a string
     */
    public static String generateSecureRandomCode(){
        SecureRandom secureRandom = new SecureRandom();
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Activa o Desactiva la autenticación en dos pasos para un usuario
     *
     * @param id Id del usuario que quiere activar o desactivar su 2FA
     * @return Un mensaje de éxito o fallo de la operación
     */
    @Transactional
    public String change2fa(Long id){
        if (id == null || id < 0){
            throw new IllegalArgumentException("El id del usuario es inválido");
        }
        User user = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        user.setIs2faEnabled(!user.getIs2faEnabled());

        return user.getIs2faEnabled()
                ? "La autenticación de dos factores ha sido activada"
                : "La autenticación de dos factores ha sido desactivada";

    }
}
