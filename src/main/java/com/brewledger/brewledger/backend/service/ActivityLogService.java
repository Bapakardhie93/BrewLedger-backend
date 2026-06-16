package com.brewledger.brewledger.backend.service;

import com.brewledger.brewledger.backend.entity.ActivityLog;
import com.brewledger.brewledger.backend.entity.User;
import com.brewledger.brewledger.backend.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @org.springframework.context.annotation.Lazy
    @org.springframework.beans.factory.annotation.Autowired
    private CurrentUserService currentUserService;

    @Transactional
    public void record(String action, String details) {
        record(action, details, null, null);
    }

    @Transactional
    public void record(String action, String details, String entityType, Long entityId) {
        String username = "SYSTEM";
        String roleName = "SYSTEM";
        try {
            User currentUser = currentUserService.requireCurrentUser();
            if (currentUser != null) {
                username = currentUser.getUsername();
                if (currentUser.getRole() != null) {
                    roleName = currentUser.getRole().getName();
                }
            }
        } catch (Exception e) {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
                    username = authentication.getName();
                    roleName = authentication.getAuthorities().stream()
                            .map(r -> r.getAuthority().replace("ROLE_", ""))
                            .findFirst()
                            .orElse("SYSTEM");
                }
            } catch (Exception ex) {
                log.warn("Failed to retrieve current user for activity log: {}", ex.getMessage());
            }
        }

        ActivityLog activityLog = new ActivityLog();
        activityLog.setUsername(username);
        activityLog.setActorRole(roleName);
        activityLog.setAction(action);
        activityLog.setDetails(details);
        activityLog.setEntityType(entityType);
        activityLog.setEntityId(entityId);

        activityLogRepository.save(activityLog);
        log.info("Audit Log recorded: User '{}' ({}), Action '{}', Details '{}', EntityType '{}', EntityId '{}'", 
                username, roleName, action, details, entityType, entityId);
    }

    @Transactional(readOnly = true)
    public List<ActivityLog> findAll() {
        return activityLogRepository.findAllByOrderByCreatedAtDesc();
    }
}
