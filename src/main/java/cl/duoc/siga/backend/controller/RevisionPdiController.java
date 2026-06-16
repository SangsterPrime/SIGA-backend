package cl.duoc.siga.backend.controller;

import cl.duoc.siga.backend.dto.request.RevisionPdiRequest;
import cl.duoc.siga.backend.dto.response.RevisionPdiResponse;
import cl.duoc.siga.backend.service.RevisionPdiService;
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
@RequestMapping("/api/revisiones-pdi")
@RequiredArgsConstructor
public class RevisionPdiController {

    private final RevisionPdiService revisionPdiService;

    @GetMapping
    public RevisionPdiResponse obtenerPorTramite(@RequestParam Long tramiteId) {
        return revisionPdiService.obtenerPorTramite(tramiteId);
    }

    @GetMapping("/{id}")
    public RevisionPdiResponse obtener(@PathVariable Long id) {
        return revisionPdiService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RevisionPdiResponse crear(@Valid @RequestBody RevisionPdiRequest request) {
        return revisionPdiService.crear(request);
    }

    @PutMapping("/{id}")
    public RevisionPdiResponse actualizar(@PathVariable Long id, @Valid @RequestBody RevisionPdiRequest request) {
        return revisionPdiService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        revisionPdiService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
