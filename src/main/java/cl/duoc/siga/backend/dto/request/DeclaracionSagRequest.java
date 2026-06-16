package cl.duoc.siga.backend.dto.request;

import cl.duoc.siga.backend.enums.ResultadoRevision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeclaracionSagRequest(
        @NotNull(message = "El trámite es obligatorio")
        Long tramiteId,

        boolean transportaProductos,

        @Size(max = 2000)
        String detalleProductos,

        Long funcionarioSagId,

        ResultadoRevision resultado
) {
}
