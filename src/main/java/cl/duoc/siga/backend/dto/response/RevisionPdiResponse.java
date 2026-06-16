package cl.duoc.siga.backend.dto.response;

import cl.duoc.siga.backend.enums.ResultadoRevision;

import java.time.OffsetDateTime;

public record RevisionPdiResponse(
        Long id,
        Long tramiteId,
        Long funcionarioPdiId,
        ResultadoRevision estadoRevision,
        String observaciones,
        OffsetDateTime fechaRevision
) {
}
