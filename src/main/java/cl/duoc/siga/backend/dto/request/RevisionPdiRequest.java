package cl.duoc.siga.backend.dto.request;

import cl.duoc.siga.backend.enums.ResultadoRevision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RevisionPdiRequest(
        @NotNull(message = "El trámite es obligatorio")
        Long tramiteId,

        Long funcionarioPdiId,

        ResultadoRevision estadoRevision,

        @Size(max = 2000)
        String observaciones
) {
}
