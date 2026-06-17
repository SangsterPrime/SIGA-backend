package cl.duoc.siga.backend.dto.response;

import cl.duoc.siga.backend.enums.AuthProvider;
import cl.duoc.siga.backend.enums.TipoUsuario;

/** Vista mínima del usuario para respuestas de auth. Nunca incluye passwordHash. */
public record AuthUserResponse(
        Long id,
        String name,
        String email,
        TipoUsuario role,
        AuthProvider provider
) {
}
