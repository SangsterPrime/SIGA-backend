package cl.duoc.siga.backend.controller;

import cl.duoc.siga.backend.dto.request.VehiculoRequest;
import cl.duoc.siga.backend.dto.response.VehiculoResponse;
import cl.duoc.siga.backend.service.VehiculoService;
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
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService vehiculoService;

    @GetMapping
    public VehiculoResponse obtenerPorTramite(@RequestParam Long tramiteId) {
        return vehiculoService.obtenerPorTramite(tramiteId);
    }

    @GetMapping("/{id}")
    public VehiculoResponse obtener(@PathVariable Long id) {
        return vehiculoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehiculoResponse crear(@Valid @RequestBody VehiculoRequest request) {
        return vehiculoService.crear(request);
    }

    @PutMapping("/{id}")
    public VehiculoResponse actualizar(@PathVariable Long id, @Valid @RequestBody VehiculoRequest request) {
        return vehiculoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
