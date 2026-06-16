package cl.duoc.siga.backend.dto.response;

import cl.duoc.siga.backend.enums.EstadoTramite;
import cl.duoc.siga.backend.enums.TipoTramite;

import java.time.OffsetDateTime;

public record TramiteResponse(
        Long id,
        String folio,
        Long pasajeroId,
        String pasajeroNombre,
        Long funcionarioAsignadoId,
        String funcionarioAsignadoNombre,
        TipoTramite tipoTramite,
        EstadoTramite estado,
        String urlComprobante,
        OffsetDateTime fechaCreacion,
        OffsetDateTime fechaActualizacion
) {
}
