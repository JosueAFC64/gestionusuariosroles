package com.lp.gestionusuariosroles.user.service;

import com.lp.gestionusuariosroles.auth.service.AuthService;
import com.lp.gestionusuariosroles.auth.service.JwtService;
import com.lp.gestionusuariosroles.exceptions.InvalidPasswordException;
import com.lp.gestionusuariosroles.exceptions.PasswordMismatchException;
import com.lp.gestionusuariosroles.user.controller.*;
import com.lp.gestionusuariosroles.user.repository.User;
import com.lp.gestionusuariosroles.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    /**
    * Devuelve los datos del usuario en sesión en formato UserDataResponse
    *
    * @param request - Cookie que contiene el JWT
    * @throws EntityNotFoundException - Si el usuario no existe
    * @return Los datos del usuario en sesión (excepto la contraseña)
    */
    @Transactional(readOnly = true)
    public UserDataResponse getUserInSessionData(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("USER_SESSION")) {
                    Claims claims = jwtService.extractPayload(cookie.getValue());
                    String email = claims.getSubject();

                    User user = repository.findByEmail(email)
                            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

                    return toDataResponse(user);
                }
            }
        }
        return null;
    }

    /**
    * Devuelve una lista de datos de todos los usuarios (excepto fechaNacimiento, telefono y contraseña)
    *
    * @return Lista de usuarios en formato {@link com.lp.gestionusuariosroles.user.controller.UserSummaryResponse}
    */
    @Transactional(readOnly = true)
    public UserSummaryResponse getAllUsers(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = repository.findAll(pageable);

        List<UserSummaryResponse.UserSummary> content = usersPage.getContent().stream()
                .map(user -> new UserSummaryResponse.UserSummary(
                        user.getId(),
                        user.getNombres(),
                        user.getApellidos(),
                        user.getEmail(),
                        user.getRol(),
                        user.getEstado()
                )).toList();

        return new UserSummaryResponse(
                content,
                usersPage.getTotalPages(),
                (int) usersPage.getTotalElements(),
                usersPage.getNumber(),
                usersPage.getSize()
        );
    }

    /**
     * Actualiza los datos de un usuario
     *
     * @param id      El id del usuario a actualizar sus datos
     * @param userDto Datos disponibles para actualizar (excepto estado y contraseña)
     */
    @Transactional
    public UserDataResponse updateUser(Long id, UserDto userDto) {

        // Validar el id y el envío de los datos
        if (id == null || id < 0) {
            throw new IllegalArgumentException("El id del usuario es inválido");
        }
        if(userDto == null) {
            throw new IllegalArgumentException("Los datos del usuario no pueden ser nulos");
        }

        // Obtener el usuario por su id
        User user = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // Validaciones los datos antes de actualizarlos
        if(userDto.nombres() != null) {
            if (userDto.nombres().isBlank()){
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
            user.setNombres(userDto.nombres());
        }

        if(userDto.apellidos() != null) {
            if (userDto.apellidos().isBlank()){
                throw new IllegalArgumentException("El apellido no puede estar vacío");
            }
            user.setApellidos(userDto.apellidos());
        }

        if(userDto.email() != null){
            if (!userDto.email().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                throw new IllegalArgumentException("Formato de email inválido");
            }
            // Solo validar si el email cambió
            if (!userDto.email().equals(user.getEmail()) && repository.existsByEmail(userDto.email())){
                throw new IllegalArgumentException("El email ya está registrado con otro usuario");
            }
            user.setEmail(userDto.email());
        }

        if(userDto.fechaNacimiento() != null){
            if (userDto.fechaNacimiento().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
            }
            user.setFechaNacimiento(userDto.fechaNacimiento());
        }

        if(userDto.telefono() != null){
            if (userDto.telefono().length() != 9){
                throw new IllegalArgumentException("El teléfono debe tener 9 dígitos");
            }
            user.setTelefono(userDto.telefono());
        }

        if(userDto.dni() != null){
            // Solo validar si el DNI cambió
            if (!userDto.dni().equals(user.getDni()) && repository.existsByDni(userDto.dni())) {
                throw new IllegalArgumentException("El dni ya está registrado con otro usuario");
            }
            user.setDni(userDto.dni());
        }

        if(userDto.rol() != null){
            user.setRol(userDto.rol());
        }

        // Guardar los cambios
        User savedUser = repository.save(user);
        return toDataResponse(savedUser);
    }

    /**
     * Devuelve los datos de un usuario por id (excepto contraseña)
     *
     * @param id El id del usuario a obtener sus datos
     * @return Datos del usuario mapeado a {@link com.lp.gestionusuariosroles.user.controller.UserDataResponse}
     */
    @Transactional(readOnly = true)
    public UserDataResponse getUserById(Long id){
        if(id == null || id < 0){
            throw new IllegalArgumentException("El id del usuario es inválido");
        }
        User user = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        return toDataResponse(user);
    }

    /**
     * Cambia el estado de un usuario (true o false)
     *
     * @param id El id del usuario a cambiar su estado
     * @param nuevoEstado El nuevo estado del usuario
     * @throws EntityNotFoundException Si el usuario no existe
     */
    @Transactional
    public void changeEstado(Long id, Boolean nuevoEstado){
        if (id == null || id < 0){
            throw new IllegalArgumentException("El id del usuario es inválido");
        }
        User user = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        user.setEstado(nuevoEstado);
        if (!nuevoEstado) {
            authService.revokeAllTokensForUser(id);
        }

    }

    @Transactional(readOnly = true)
    public RolDistributionResponse getRolDistribution() {
        List<Object[]> results = repository.countUsersByRol();
        Map<String, Long> distribution = new HashMap<>();

        for (Object[] result : results) {
            String rol = (String) result[0];
            Long count = (Long) result[1];
            distribution.put(rol, count);
        }

        // Asegurarse de que todos los roles estén presentes, incluso si no hay usuarios
        if (!distribution.containsKey("ADMINISTRADOR")) {
            distribution.put("ADMINISTRADOR", 0L);
        }
        if (!distribution.containsKey("SUPERVISOR")) {
            distribution.put("SUPERVISOR", 0L);
        }
        if (!distribution.containsKey("EGRESADO")) {
            distribution.put("EGRESADO", 0L);
        }

        return new RolDistributionResponse(distribution);
    }

    /**
     * Cambia la contraseña de un usuario
     *
     * @param request Para obtener la cookie y obtener el usuario a cambiar su contraseña
     * @param passwordRequest Datos requeridos para cambiar la contraseña (currentPassword y newPassword)
     * @return Un {@link UpdatePasswordResponse} que devolverá el éxito o el fallo de la operación
     */
    @Transactional
    public UpdatePasswordResponse updatePassword(HttpServletRequest request, UpdatePasswordRequest passwordRequest){
        User user = repository.findById(getUserInSessionData(request).id())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        if(!passwordEncoder.matches(passwordRequest.currentPassword(), user.getPassword())){
            throw new InvalidPasswordException("La contraseña actual es incorrecta");
        }
        if(passwordEncoder.matches(passwordRequest.newPassword(), user.getPassword())){
            throw new PasswordMismatchException("La nueva contraseña no puede ser igual a la actual");
        }
        if (!passwordRequest.newPassword().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
            throw new InvalidPasswordException("La contraseña debe tener al menos 8 caracteres, una mayúscula, un número y un carácter especial");
        }

        String newPassword = passwordEncoder.encode(passwordRequest.newPassword());
        user.setPassword(newPassword);

        return new UpdatePasswordResponse("Contraseña actualizada correctamente", true);
    }

    /**
     * Elimina a un usuario de la Base de Datos
     *
     * @param id Id del usuario a eliminar
     * @param request Objeto HttpServletRequest que contiene las cookies
     * @param response Objeto HttpServletResponse
     */
    @Transactional
    public void deleteUser(Long id, HttpServletRequest request, HttpServletResponse response){

        if (id == null || id < 0) {
            throw new IllegalArgumentException("El id del usuario es inválido");
        }

        UserDataResponse userInSession = getUserInSessionData(request);
        User user = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        repository.delete(user);

        if (id.equals(userInSession.id())) {
            invalidateSessionCookie(request, response);
        }
    }

    /**
    * Convierte un objeto {@link com.lp.gestionusuariosroles.user.repository.User} a uno {@link com.lp.gestionusuariosroles.user.controller.UserDataResponse}
    *
    * @param user Objeto {@link com.lp.gestionusuariosroles.user.repository.User}
    * @return Objeto {@link com.lp.gestionusuariosroles.user.controller.UserDataResponse}
    */
    private UserDataResponse toDataResponse(User user) {
        return new UserDataResponse(
                user.getId(),
                user.getNombres(),
                user.getApellidos(),
                user.getEmail(),
                user.getRol(),
                user.getFechaNacimiento(),
                user.getEstado(),
                user.getTelefono(),
                user.getDni(),
                user.getIs2faEnabled()
        );
    }

    /**
     * Invalida y elimina las cookies del usuario eliminado en el servicio deleteUser
     *
     * @param request Objeto HttpServletRequest que contiene las cookies
     * @param response Objeto HttpServletResponse
     */
    private void invalidateSessionCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("USER_SESSION")) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    break;
                }
            }
        }
    }

}
