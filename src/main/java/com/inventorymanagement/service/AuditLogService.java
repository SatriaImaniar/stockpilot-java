package com.inventorymanagement.service;

import com.inventorymanagement.entity.AuditLog;
import com.inventorymanagement.entity.User;
import com.inventorymanagement.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(User user, String action, String entityType, Long entityId, String details) {
        AuditLog auditLog = new AuditLog(user, action, entityType, entityId, details);
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByFilters(String entityType, Long userId,
                                                LocalDateTime startDate, LocalDateTime endDate,
                                                Pageable pageable) {
        return auditLogRepository.findByFilters(entityType, userId, startDate, endDate, pageable);
    }
}

