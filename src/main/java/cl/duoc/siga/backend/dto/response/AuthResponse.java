package cl.duoc.siga.backend.dto.response;

/**
 * Respuesta unificada de autenticación (registro/login manual).
 *
 * <p>La autenticación es por sesión (cookie {@code JSESSIONID}), el mismo mecanismo que
 * Google OAuth. {@code token} expone el id de sesión como identificador informativo;
 * el frontend autentica las llamadas siguientes con {@code credentials: "include"}.</p>
 */
public record AuthResponse(
        String token,
        AuthUserResponse user
) {
}
