package cl.duoc.siga.backend.service;

import cl.duoc.siga.backend.dto.request.CambiarEstadoRequest;
import cl.duoc.siga.backend.dto.request.TramiteRequest;
import cl.duoc.siga.backend.dto.response.HistorialResponse;
import cl.duoc.siga.backend.dto.response.TramiteResponse;
import cl.duoc.siga.backend.enums.EstadoTramite;
import cl.duoc.siga.backend.exception.ConflictException;
import cl.duoc.siga.backend.exception.ResourceNotFoundException;
import cl.duoc.siga.backend.mapper.HistorialMapper;
import cl.duoc.siga.backend.mapper.TramiteMapper;
import cl.duoc.siga.backend.model.HistorialEstadoTramite;
import cl.duoc.siga.backend.model.TramiteAduanero;
import cl.duoc.siga.backend.model.Usuario;
import cl.duoc.siga.backend.repository.HistorialEstadoTramiteRepository;
import cl.duoc.siga.backend.repository.TramiteAduaneroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TramiteService {

    private final TramiteAduaneroRepository tramiteRepository;
    private final HistorialEstadoTramiteRepository historialRepository;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public List<TramiteResponse> listar(EstadoTramite estado, Long pasajeroId) {
        List<TramiteAduanero> tramites;
        if (estado != null) {
            tramites = tramiteRepository.findByEstado(estado);
        } else if (pasajeroId != null) {
            tramites = tramiteRepository.findByPasajeroId(pasajeroId);
        } else {
            tramites = tramiteRepository.findAll();
        }
        return tramites.stream().map(TramiteMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TramiteResponse obtener(Long id) {
        return TramiteMapper.toResponse(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public TramiteAduanero obtenerEntidad(Long id) {
        return tramiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite", id));
    }

    public TramiteResponse crear(TramiteRequest req) {
        Usuario pasajero = usuarioService.obtenerEntidad(req.pasajeroId());

        TramiteAduanero t = new TramiteAduanero();
        t.setFolio(resolverFolio(req.folio()));
        t.setPasajero(pasajero);
        if (req.funcionarioAsignadoId() != null) {
            t.setFuncionarioAsignado(usuarioService.obtenerEntidad(req.funcionarioAsignadoId()));
        }
        t.setTipoTramite(req.tipoTramite());
        t.setEstado(EstadoTramite.BORRADOR);
        TramiteAduanero guardado = tramiteRepository.save(t);

        registrarHistorial(guardado, null, EstadoTramite.BORRADOR, null, "Trámite creado");
        return TramiteMapper.toResponse(guardado);
    }

    public TramiteResponse actualizar(Long id, TramiteRequest req) {
        TramiteAduanero t = obtenerEntidad(id);
        if (req.folio() != null && !req.folio().isBlank() && !req.folio().equals(t.getFolio())) {
            if (tramiteRepository.existsByFolio(req.folio())) {
                throw new ConflictException("Ya existe un trámite con el folio: " + req.folio());
            }
            t.setFolio(req.folio());
        }
        t.setPasajero(usuarioService.obtenerEntidad(req.pasajeroId()));
        if (req.funcionarioAsignadoId() != null) {
            t.setFuncionarioAsignado(usuarioService.obtenerEntidad(req.funcionarioAsignadoId()));
        } else {
            t.setFuncionarioAsignado(null);
        }
        t.setTipoTramite(req.tipoTramite());
        return TramiteMapper.toResponse(t);
    }

    /** Cambia el estado del trámite y registra el cambio en el historial (trazabilidad). */
    public TramiteResponse cambiarEstado(Long id, CambiarEstadoRequest req) {
        TramiteAduanero t = obtenerEntidad(id);
        EstadoTramite anterior = t.getEstado();
        EstadoTramite nuevo = req.estadoNuevo();

        t.setEstado(nuevo);
        if (nuevo == EstadoTramite.APROBADO && t.getUrlComprobante() == null) {
            t.setUrlComprobante("/comprobantes/" + t.getFolio() + ".pdf");
        }

        Usuario funcionario = req.funcionarioId() != null
                ? usuarioService.obtenerEntidad(req.funcionarioId())
                : null;
        registrarHistorial(t, anterior, nuevo, funcionario, req.comentario());
        return TramiteMapper.toResponse(t);
    }

    public void eliminar(Long id) {
        TramiteAduanero t = obtenerEntidad(id);
        tramiteRepository.delete(t);
    }

    @Transactional(readOnly = true)
    public List<HistorialResponse> historial(Long tramiteId) {
        obtenerEntidad(tramiteId); // valida existencia
        return historialRepository.findByTramiteIdOrderByFechaCambioAsc(tramiteId)
                .stream().map(HistorialMapper::toResponse).toList();
    }

    private void registrarHistorial(TramiteAduanero tramite, EstadoTramite anterior,
                                    EstadoTramite nuevo, Usuario funcionario, String comentario) {
        HistorialEstadoTramite h = new HistorialEstadoTramite();
        h.setTramite(tramite);
        h.setEstadoAnterior(anterior);
        h.setEstadoNuevo(nuevo);
        h.setFuncionario(funcionario);
        h.setComentario(comentario);
        historialRepository.save(h);
    }

    private String resolverFolio(String folioSolicitado) {
        if (folioSolicitado != null && !folioSolicitado.isBlank()) {
            if (tramiteRepository.existsByFolio(folioSolicitado)) {
                throw new ConflictException("Ya existe un trámite con el folio: " + folioSolicitado);
            }
            return folioSolicitado;
        }
        String folio = "SIGA-" + OffsetDateTime.now().toInstant().toEpochMilli();
        while (tramiteRepository.existsByFolio(folio)) {
            folio = "SIGA-" + System.nanoTime();
        }
        return folio;
    }
}
