package cl.duoc.siga.backend.mapper;

import cl.duoc.siga.backend.dto.response.TramiteResponse;
import cl.duoc.siga.backend.model.TramiteAduanero;
import cl.duoc.siga.backend.model.Usuario;

/** Conversión entre la entidad TramiteAduanero y sus DTOs. */
public final class TramiteMapper {

    private TramiteMapper() {
    }

    public static TramiteResponse toResponse(TramiteAduanero t) {
        Usuario pasajero = t.getPasajero();
        Usuario funcionario = t.getFuncionarioAsignado();
        return new TramiteResponse(
                t.getId(),
                t.getFolio(),
                pasajero != null ? pasajero.getId() : null,
                pasajero != null ? pasajero.getNombre() : null,
                funcionario != null ? funcionario.getId() : null,
                funcionario != null ? funcionario.getNombre() : null,
                t.getTipoTramite(),
                t.getEstado(),
                t.getUrlComprobante(),
                t.getFechaCreacion(),
                t.getFechaActualizacion());
    }
}
