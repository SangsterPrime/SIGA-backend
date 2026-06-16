package cl.duoc.siga.backend.service;

import cl.duoc.siga.backend.dto.request.DeclaracionSagRequest;
import cl.duoc.siga.backend.dto.response.DeclaracionSagResponse;
import cl.duoc.siga.backend.enums.ResultadoRevision;
import cl.duoc.siga.backend.exception.ConflictException;
import cl.duoc.siga.backend.exception.ResourceNotFoundException;
import cl.duoc.siga.backend.mapper.DeclaracionSagMapper;
import cl.duoc.siga.backend.model.DeclaracionSag;
import cl.duoc.siga.backend.repository.DeclaracionSagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class DeclaracionSagService {

    private final DeclaracionSagRepository declaracionRepository;
    private final TramiteService tramiteService;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public DeclaracionSagResponse obtener(Long id) {
        return DeclaracionSagMapper.toResponse(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public DeclaracionSagResponse obtenerPorTramite(Long tramiteId) {
        return declaracionRepository.findByTramiteId(tramiteId)
                .map(DeclaracionSagMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe declaración SAG para el trámite: " + tramiteId));
    }

    public DeclaracionSagResponse crear(DeclaracionSagRequest req) {
        if (declaracionRepository.existsByTramiteId(req.tramiteId())) {
            throw new ConflictException("El trámite " + req.tramiteId() + " ya tiene una declaración SAG");
        }
        DeclaracionSag d = new DeclaracionSag();
        d.setTramite(tramiteService.obtenerEntidad(req.tramiteId()));
        aplicar(d, req);
        return DeclaracionSagMapper.toResponse(declaracionRepository.save(d));
    }

    public DeclaracionSagResponse actualizar(Long id, DeclaracionSagRequest req) {
        DeclaracionSag d = obtenerEntidad(id);
        aplicar(d, req);
        return DeclaracionSagMapper.toResponse(d);
    }

    public void eliminar(Long id) {
        declaracionRepository.delete(obtenerEntidad(id));
    }

    private void aplicar(DeclaracionSag d, DeclaracionSagRequest req) {
        d.setTransportaProductos(req.transportaProductos());
        d.setDetalleProductos(req.detalleProductos());
        d.setFuncionarioSag(req.funcionarioSagId() != null
                ? usuarioService.obtenerEntidad(req.funcionarioSagId())
                : null);
        if (req.resultado() != null) {
            d.setResultado(req.resultado());
            if (req.resultado() != ResultadoRevision.PENDIENTE) {
                d.setFechaRevision(OffsetDateTime.now());
            }
        }
    }

    private DeclaracionSag obtenerEntidad(Long id) {
        return declaracionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Declaración SAG", id));
    }
}
