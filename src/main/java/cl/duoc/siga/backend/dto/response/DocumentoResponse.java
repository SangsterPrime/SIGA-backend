package cl.duoc.siga.backend.dto.response;

import cl.duoc.siga.backend.enums.TipoDocumento;

import java.time.OffsetDateTime;

public record DocumentoResponse(
        Long id,
        Long tramiteId,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String urlArchivo,
        OffsetDateTime fechaCarga
) {
}
