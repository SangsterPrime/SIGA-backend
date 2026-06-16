package cl.duoc.siga.backend.dto.request;

import cl.duoc.siga.backend.enums.EstadoTramite;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(
        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoTramite estadoNuevo,

        Long funcionarioId,

        String comentario
) {
}
