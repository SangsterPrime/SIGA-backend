package cl.duoc.siga.backend.controller;

import cl.duoc.siga.backend.dto.request.DocumentoRequest;
import cl.duoc.siga.backend.dto.response.DocumentoResponse;
import cl.duoc.siga.backend.service.DocumentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    @GetMapping
    public List<DocumentoResponse> listarPorTramite(@RequestParam Long tramiteId) {
        return documentoService.listarPorTramite(tramiteId);
    }

    @GetMapping("/{id}")
    public DocumentoResponse obtener(@PathVariable Long id) {
        return documentoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentoResponse crear(@Valid @RequestBody DocumentoRequest request) {
        return documentoService.crear(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        documentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
