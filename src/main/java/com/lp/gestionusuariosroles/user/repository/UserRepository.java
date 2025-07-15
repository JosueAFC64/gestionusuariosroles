package com.lp.gestionusuariosroles.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u.rol, COUNT(u) FROM User u GROUP BY u.rol")
    List<Object[]> countUsersByRol();

    User findByResetToken(String resetToken);

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    long countUsersByEstado(Boolean estado);
}
