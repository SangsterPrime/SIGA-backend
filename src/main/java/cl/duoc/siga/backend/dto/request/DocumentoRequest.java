package cl.duoc.siga.backend.dto.request;

import cl.duoc.siga.backend.enums.TipoDocumento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DocumentoRequest(
        @NotNull(message = "El trámite es obligatorio")
        Long tramiteId,

        @NotNull(message = "El tipo de documento es obligatorio")
        TipoDocumento tipoDocumento,

        @Size(max = 50)
        String numeroDocumento,

        @Size(max = 500)
        String urlArchivo
) {
}
