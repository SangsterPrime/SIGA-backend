package cl.duoc.siga.backend.mapper;

import cl.duoc.siga.backend.dto.response.HistorialResponse;
import cl.duoc.siga.backend.model.HistorialEstadoTramite;

public final class HistorialMapper {

    private HistorialMapper() {
    }

    public static HistorialResponse toResponse(HistorialEstadoTramite h) {
        return new HistorialResponse(
                h.getId(),
                h.getTramite() != null ? h.getTramite().getId() : null,
                h.getEstadoAnterior(),
                h.getEstadoNuevo(),
                h.getFuncionario() != null ? h.getFuncionario().getId() : null,
                h.getComentario(),
                h.getFechaCambio());
    }
}
