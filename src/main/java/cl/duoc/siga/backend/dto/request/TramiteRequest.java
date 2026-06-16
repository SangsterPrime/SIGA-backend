package cl.duoc.siga.backend.dto.request;

import cl.duoc.siga.backend.enums.TipoTramite;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TramiteRequest(
        @Size(max = 20, message = "El folio no puede superar 20 caracteres")
        String folio,

        @NotNull(message = "El pasajero es obligatorio")
        Long pasajeroId,

        Long funcionarioAsignadoId,

        @NotNull(message = "El tipo de trámite es obligatorio")
        TipoTramite tipoTramite
) {
}
