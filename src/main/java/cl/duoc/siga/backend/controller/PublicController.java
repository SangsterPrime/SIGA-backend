package cl.duoc.siga.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

/** Endpoints públicos (no requieren autenticación). */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "timestamp", OffsetDateTime.now());
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
                "app", "SIGA Backend",
                "descripcion", "Sistema Integrado de Gestión Aduanera - MVP",
                "version", "0.0.1-SNAPSHOT");
    }
}
