package cl.duoc.siga.backend.mapper;

import cl.duoc.siga.backend.dto.response.DocumentoResponse;
import cl.duoc.siga.backend.model.Documento;

public final class DocumentoMapper {

    private DocumentoMapper() {
    }

    public static DocumentoResponse toResponse(Documento d) {
        return new DocumentoResponse(
                d.getId(),
                d.getTramite() != null ? d.getTramite().getId() : null,
                d.getTipoDocumento(),
                d.getNumeroDocumento(),
                d.getUrlArchivo(),
                d.getFechaCarga());
    }
}
