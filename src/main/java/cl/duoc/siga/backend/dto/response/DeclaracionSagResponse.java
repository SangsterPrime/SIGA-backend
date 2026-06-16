package cl.duoc.siga.backend.dto.response;

import cl.duoc.siga.backend.enums.ResultadoRevision;

import java.time.OffsetDateTime;

public record DeclaracionSagResponse(
        Long id,
        Long tramiteId,
        boolean transportaProductos,
        String detalleProductos,
        Long funcionarioSagId,
        ResultadoRevision resultado,
        OffsetDateTime fechaRevision
) {
}
