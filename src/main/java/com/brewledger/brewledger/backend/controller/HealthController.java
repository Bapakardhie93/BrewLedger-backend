package com.brewledger.brewledger.backend.controller;

import com.brewledger.brewledger.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final UserRepository userRepository;

    @GetMapping
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        String dbStatus = "DOWN";
        try {
            userRepository.count();
            dbStatus = "UP";
        } catch (Exception e) {
            // Log if needed
        }

        response.put("status", dbStatus);
        response.put("database", dbStatus);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }
}
