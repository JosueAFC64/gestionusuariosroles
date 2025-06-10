package com.lp.gestionusuariosroles.activitylog.service;

import com.lp.gestionusuariosroles.activitylog.contoller.ActivityLogDto;
import com.lp.gestionusuariosroles.activitylog.contoller.LogDto;
import com.lp.gestionusuariosroles.activitylog.repository.ActivityLog;
import com.lp.gestionusuariosroles.activitylog.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository repository;

    @Transactional
    public void logActivity(Long userId, String username, String action, String description) {
        ActivityLog log = ActivityLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .description(description)
                .build();
        repository.save(log);
    }

    @Transactional(readOnly = true)
    public ActivityLogDto getAllLogs (int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("actionDateTime").descending());
        Page<ActivityLog> logsPage = repository.findAll(pageable);

        List<LogDto> content = logsPage.getContent().stream()
                .map(log -> new LogDto(
                        log.getUsername(),
                        log.getAction(),
                        log.getDescription(),
                        log.getActionDateTime()
                )).toList();

        return new ActivityLogDto(
                content,
                logsPage.getTotalPages(),
                (int) logsPage.getTotalElements(),
                logsPage.getNumber(),
                logsPage.getSize()
        );
    }

}
