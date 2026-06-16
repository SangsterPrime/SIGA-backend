package cl.duoc.siga.backend.service;

import cl.duoc.siga.backend.dto.request.RevisionPdiRequest;
import cl.duoc.siga.backend.dto.response.RevisionPdiResponse;
import cl.duoc.siga.backend.enums.ResultadoRevision;
import cl.duoc.siga.backend.exception.ConflictException;
import cl.duoc.siga.backend.exception.ResourceNotFoundException;
import cl.duoc.siga.backend.mapper.RevisionPdiMapper;
import cl.duoc.siga.backend.model.RevisionPdi;
import cl.duoc.siga.backend.repository.RevisionPdiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RevisionPdiService {

    private final RevisionPdiRepository revisionRepository;
    private final TramiteService tramiteService;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public RevisionPdiResponse obtener(Long id) {
        return RevisionPdiMapper.toResponse(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public RevisionPdiResponse obtenerPorTramite(Long tramiteId) {
        return revisionRepository.findByTramiteId(tramiteId)
                .map(RevisionPdiMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe revisión PDI para el trámite: " + tramiteId));
    }

    public RevisionPdiResponse crear(RevisionPdiRequest req) {
        if (revisionRepository.existsByTramiteId(req.tramiteId())) {
            throw new ConflictException("El trámite " + req.tramiteId() + " ya tiene una revisión PDI");
        }
        RevisionPdi r = new RevisionPdi();
        r.setTramite(tramiteService.obtenerEntidad(req.tramiteId()));
        aplicar(r, req);
        return RevisionPdiMapper.toResponse(revisionRepository.save(r));
    }

    public RevisionPdiResponse actualizar(Long id, RevisionPdiRequest req) {
        RevisionPdi r = obtenerEntidad(id);
        aplicar(r, req);
        return RevisionPdiMapper.toResponse(r);
    }

    public void eliminar(Long id) {
        revisionRepository.delete(obtenerEntidad(id));
    }

    private void aplicar(RevisionPdi r, RevisionPdiRequest req) {
        r.setFuncionarioPdi(req.funcionarioPdiId() != null
                ? usuarioService.obtenerEntidad(req.funcionarioPdiId())
                : null);
        r.setObservaciones(req.observaciones());
        if (req.estadoRevision() != null) {
            r.setEstadoRevision(req.estadoRevision());
            if (req.estadoRevision() != ResultadoRevision.PENDIENTE) {
                r.setFechaRevision(OffsetDateTime.now());
            }
        }
    }

    private RevisionPdi obtenerEntidad(Long id) {
        return revisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Revisión PDI", id));
    }
}
