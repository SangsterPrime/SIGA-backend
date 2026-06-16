package cl.duoc.siga.backend.service;

import cl.duoc.siga.backend.dto.request.VehiculoRequest;
import cl.duoc.siga.backend.dto.response.VehiculoResponse;
import cl.duoc.siga.backend.exception.ConflictException;
import cl.duoc.siga.backend.exception.ResourceNotFoundException;
import cl.duoc.siga.backend.mapper.VehiculoMapper;
import cl.duoc.siga.backend.model.Vehiculo;
import cl.duoc.siga.backend.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final TramiteService tramiteService;

    @Transactional(readOnly = true)
    public VehiculoResponse obtener(Long id) {
        return VehiculoMapper.toResponse(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public VehiculoResponse obtenerPorTramite(Long tramiteId) {
        return vehiculoRepository.findByTramiteId(tramiteId)
                .map(VehiculoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe vehículo para el trámite: " + tramiteId));
    }

    public VehiculoResponse crear(VehiculoRequest req) {
        if (vehiculoRepository.existsByTramiteId(req.tramiteId())) {
            throw new ConflictException("El trámite " + req.tramiteId() + " ya tiene un vehículo declarado");
        }
        Vehiculo v = new Vehiculo();
        v.setTramite(tramiteService.obtenerEntidad(req.tramiteId()));
        aplicar(v, req);
        return VehiculoMapper.toResponse(vehiculoRepository.save(v));
    }

    public VehiculoResponse actualizar(Long id, VehiculoRequest req) {
        Vehiculo v = obtenerEntidad(id);
        aplicar(v, req);
        return VehiculoMapper.toResponse(v);
    }

    public void eliminar(Long id) {
        vehiculoRepository.delete(obtenerEntidad(id));
    }

    private void aplicar(Vehiculo v, VehiculoRequest req) {
        v.setPatente(req.patente());
        v.setMarca(req.marca());
        v.setModelo(req.modelo());
        v.setAnio(req.anio());
        v.setPaisRegistro(req.paisRegistro());
        v.setTipoFormulario(req.tipoFormulario());
    }

    private Vehiculo obtenerEntidad(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", id));
    }
}
