package cl.duoc.siga.backend.mapper;

import cl.duoc.siga.backend.dto.response.RevisionPdiResponse;
import cl.duoc.siga.backend.model.RevisionPdi;

public final class RevisionPdiMapper {

    private RevisionPdiMapper() {
    }

    public static RevisionPdiResponse toResponse(RevisionPdi r) {
        return new RevisionPdiResponse(
                r.getId(),
                r.getTramite() != null ? r.getTramite().getId() : null,
                r.getFuncionarioPdi() != null ? r.getFuncionarioPdi().getId() : null,
                r.getEstadoRevision(),
                r.getObservaciones(),
                r.getFechaRevision());
    }
}
