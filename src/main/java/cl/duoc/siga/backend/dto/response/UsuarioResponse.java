package cl.duoc.siga.backend.dto.response;

import cl.duoc.siga.backend.enums.EstadoCuenta;
import cl.duoc.siga.backend.enums.RolFuncionario;
import cl.duoc.siga.backend.enums.TipoUsuario;

import java.time.OffsetDateTime;

public record UsuarioResponse(
        Long id,
        String rut,
        String nombre,
        String correo,
        TipoUsuario tipoUsuario,
        RolFuncionario rolFuncionario,
        EstadoCuenta estadoCuenta,
        boolean dosFaHabilitado,
        OffsetDateTime fechaCreacion
) {
}
