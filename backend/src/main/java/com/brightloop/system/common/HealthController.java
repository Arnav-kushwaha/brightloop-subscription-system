package com.brightloop.system.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check controller for system monitoring and frontend connectivity verification.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${spring.datasource.url:not-configured}")
    private String datasourceUrl;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealthStatus() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("status", "UP");
        details.put("service", "Brightloop Subscription Management System");
        details.put("version", "1.0.0-SNAPSHOT");
        details.put("timestamp", LocalDateTime.now());
        details.put("javaVersion", System.getProperty("java.version"));
        details.put("activeProfile", activeProfile);
        details.put("databaseTarget", sanitizeDatabaseUrl(datasourceUrl));

        ApiResponse<Map<String, Object>> response = ApiResponse.success(details, "Brightloop Backend is active and running");
        return ResponseEntity.ok(response);
    }

    private String sanitizeDatabaseUrl(String url) {
        if (url == null || !url.contains("@")) {
            return url;
        }
        // Mask passwords in URL if present
        return url.replaceAll("://.*@", "://***:***@");
    }
}
