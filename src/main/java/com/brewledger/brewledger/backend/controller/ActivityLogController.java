package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.entity.ActivityLog;
import com.brewledger.brewledger.backend.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activity-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGEMENT')")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public List<ActivityLog> findAll() {
        return activityLogService.findAll();
    }
}
