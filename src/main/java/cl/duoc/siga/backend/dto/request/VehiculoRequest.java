package cl.duoc.siga.backend.dto.request;

import cl.duoc.siga.backend.enums.TipoFormulario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehiculoRequest(
        @NotNull(message = "El trámite es obligatorio")
        Long tramiteId,

        @NotBlank(message = "La patente es obligatoria")
        @Size(max = 10)
        String patente,

        @Size(max = 60)
        String marca,

        @Size(max = 60)
        String modelo,

        Integer anio,

        @Size(max = 40)
        String paisRegistro,

        @NotNull(message = "El tipo de formulario es obligatorio")
        TipoFormulario tipoFormulario
) {
}
