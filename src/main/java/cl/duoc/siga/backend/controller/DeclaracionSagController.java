package cl.duoc.siga.backend.controller;

import cl.duoc.siga.backend.dto.request.DeclaracionSagRequest;
import cl.duoc.siga.backend.dto.response.DeclaracionSagResponse;
import cl.duoc.siga.backend.service.DeclaracionSagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/declaraciones-sag")
@RequiredArgsConstructor
public class DeclaracionSagController {

    private final DeclaracionSagService declaracionSagService;

    @GetMapping
    public DeclaracionSagResponse obtenerPorTramite(@RequestParam Long tramiteId) {
        return declaracionSagService.obtenerPorTramite(tramiteId);
    }

    @GetMapping("/{id}")
    public DeclaracionSagResponse obtener(@PathVariable Long id) {
        return declaracionSagService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeclaracionSagResponse crear(@Valid @RequestBody DeclaracionSagRequest request) {
        return declaracionSagService.crear(request);
    }

    @PutMapping("/{id}")
    public DeclaracionSagResponse actualizar(@PathVariable Long id, @Valid @RequestBody DeclaracionSagRequest request) {
        return declaracionSagService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        declaracionSagService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
