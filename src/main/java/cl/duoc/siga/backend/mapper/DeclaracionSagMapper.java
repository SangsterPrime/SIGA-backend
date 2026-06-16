package cl.duoc.siga.backend.mapper;

import cl.duoc.siga.backend.dto.response.DeclaracionSagResponse;
import cl.duoc.siga.backend.model.DeclaracionSag;

public final class DeclaracionSagMapper {

    private DeclaracionSagMapper() {
    }

    public static DeclaracionSagResponse toResponse(DeclaracionSag d) {
        return new DeclaracionSagResponse(
                d.getId(),
                d.getTramite() != null ? d.getTramite().getId() : null,
                d.isTransportaProductos(),
                d.getDetalleProductos(),
                d.getFuncionarioSag() != null ? d.getFuncionarioSag().getId() : null,
                d.getResultado(),
                d.getFechaRevision());
    }
}
