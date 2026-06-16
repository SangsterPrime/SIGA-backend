package cl.duoc.siga.backend.mapper;

import cl.duoc.siga.backend.dto.response.UsuarioResponse;
import cl.duoc.siga.backend.model.Usuario;

/** Conversión entre la entidad Usuario y sus DTOs. */
public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getRut(),
                u.getNombre(),
                u.getCorreo(),
                u.getTipoUsuario(),
                u.getRolFuncionario(),
                u.getEstadoCuenta(),
                u.isDosFaHabilitado(),
                u.getFechaCreacion());
    }
}
