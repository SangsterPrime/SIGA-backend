package cl.duoc.siga.backend.controller;

import cl.duoc.siga.backend.dto.request.CambiarEstadoRequest;
import cl.duoc.siga.backend.dto.request.TramiteRequest;
import cl.duoc.siga.backend.dto.response.DocumentoResponse;
import cl.duoc.siga.backend.dto.response.HistorialResponse;
import cl.duoc.siga.backend.dto.response.TramiteResponse;
import cl.duoc.siga.backend.enums.EstadoTramite;
import cl.duoc.siga.backend.service.DocumentoService;
import cl.duoc.siga.backend.service.TramiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tramites")
@RequiredArgsConstructor
public class TramiteController {

    private final TramiteService tramiteService;
    private final DocumentoService documentoService;

    @GetMapping
    public List<TramiteResponse> listar(
            @RequestParam(required = false) EstadoTramite estado,
            @RequestParam(required = false) Long pasajeroId) {
        return tramiteService.listar(estado, pasajeroId);
    }

    @GetMapping("/{id}")
    public TramiteResponse obtener(@PathVariable Long id) {
        return tramiteService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TramiteResponse crear(@Valid @RequestBody TramiteRequest request) {
        return tramiteService.crear(request);
    }

    @PutMapping("/{id}")
    public TramiteResponse actualizar(@PathVariable Long id, @Valid @RequestBody TramiteRequest request) {
        return tramiteService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public TramiteResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest request) {
        return tramiteService.cambiarEstado(id, request);
    }

    @GetMapping("/{id}/historial")
    public List<HistorialResponse> historial(@PathVariable Long id) {
        return tramiteService.historial(id);
    }

    @GetMapping("/{id}/documentos")
    public List<DocumentoResponse> documentos(@PathVariable Long id) {
        return documentoService.listarPorTramite(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tramiteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
