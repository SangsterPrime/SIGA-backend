package cl.duoc.siga.backend.controller;

import cl.duoc.siga.backend.dto.response.UsuarioResponse;
import cl.duoc.siga.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint del usuario autenticado. Funciona para AMBOS métodos de login:
 * Google OAuth2 (principal {@link OidcUser}) y login manual (principal = correo en un
 * token usuario/contraseña). En ambos casos la sesión viaja en la cookie {@code JSESSIONID}.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(Authentication authentication) {
        String correo = resolverCorreo(authentication);
        if (correo == null) {
            log.warn("/api/me -> sin correo resoluble: no llegó sesión válida (revisar cookie JSESSIONID).");
            return ResponseEntity.status(401).build();
        }
        log.info("/api/me -> authenticated=true, email={}", correo);
        return ResponseEntity.ok(usuarioService.obtenerPorCorreo(correo));
    }

    /** Extrae el correo del principal, sea login con Google (OIDC) o manual. */
    private String resolverCorreo(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if (authentication.getPrincipal() instanceof OidcUser oidc) {
            Object email = oidc.getClaims().get("email");
            return email != null ? email.toString() : null;
        }
        // Login manual: el principal es el correo (String).
        String name = authentication.getName();
        return (name != null && !name.isBlank()) ? name : null;
    }
}
