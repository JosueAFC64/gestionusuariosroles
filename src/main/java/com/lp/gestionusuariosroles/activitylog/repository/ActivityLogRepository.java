package com.lp.gestionusuariosroles.activitylog.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByUserIdOrderByActionDateTimeDesc(Long userId);

}
