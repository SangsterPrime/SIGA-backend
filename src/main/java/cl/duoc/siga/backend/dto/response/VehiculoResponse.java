package cl.duoc.siga.backend.dto.response;

import cl.duoc.siga.backend.enums.TipoFormulario;

public record VehiculoResponse(
        Long id,
        Long tramiteId,
        String patente,
        String marca,
        String modelo,
        Integer anio,
        String paisRegistro,
        TipoFormulario tipoFormulario
) {
}
