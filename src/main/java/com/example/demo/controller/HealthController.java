package com.example.demo.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {
    private final JdbcTemplate jdbc;
    public HealthController(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Integer result = jdbc.queryForObject("SELECT 1", Integer.class);
        return Map.of("ok", result != null && result == 1, "database", "MySQL", "message", "API conectada a MySQL");
    }
}
