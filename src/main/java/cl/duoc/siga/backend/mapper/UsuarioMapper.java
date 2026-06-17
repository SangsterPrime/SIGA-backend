package cl.duoc.siga.backend.mapper;

import cl.duoc.siga.backend.dto.response.AuthUserResponse;
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

    /** Vista mínima para respuestas de auth (sin passwordHash). El rol es {@code tipoUsuario}. */
    public static AuthUserResponse toAuthUser(Usuario u) {
        return new AuthUserResponse(
                u.getId(),
                u.getNombre(),
                u.getCorreo(),
                u.getTipoUsuario(),
                u.getProvider());
    }
}
