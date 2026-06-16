package cl.duoc.siga.backend.dto.response;

import cl.duoc.siga.backend.enums.EstadoTramite;

import java.time.OffsetDateTime;

public record HistorialResponse(
        Long id,
        Long tramiteId,
        EstadoTramite estadoAnterior,
        EstadoTramite estadoNuevo,
        Long funcionarioId,
        String comentario,
        OffsetDateTime fechaCambio
) {
}
