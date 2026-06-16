package cl.duoc.siga.backend.mapper;

import cl.duoc.siga.backend.dto.response.VehiculoResponse;
import cl.duoc.siga.backend.model.Vehiculo;

public final class VehiculoMapper {

    private VehiculoMapper() {
    }

    public static VehiculoResponse toResponse(Vehiculo v) {
        return new VehiculoResponse(
                v.getId(),
                v.getTramite() != null ? v.getTramite().getId() : null,
                v.getPatente(),
                v.getMarca(),
                v.getModelo(),
                v.getAnio(),
                v.getPaisRegistro(),
                v.getTipoFormulario());
    }
}
