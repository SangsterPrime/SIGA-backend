package cl.duoc.siga.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Maneja {@code GET /login} de forma segura.
 *
 * <p>Spring Security redirige a {@code /login?error} cuando el callback OAuth2 falla
 * (caso típico en móvil: la cookie de la <em>authorization request</em> se bloquea por
 * políticas cross-site, y la respuesta de Google no encuentra la petición guardada en
 * sesión). El backend no sirve ninguna página {@code /login}, así que sin esto la
 * respuesta es un 500 «No static resource login for request '/login'».</p>
 *
 * <p>Aquí redirigimos {@code /login} al login del frontend con marca de error, en lugar
 * de a {@code /oauth2/authorization/google}, para no entrar en un bucle de reintento si
 * el fallo es persistente. El callback real {@code /login/oauth2/code/google} lo procesa
 * el filtro de Spring Security antes del dispatcher, así que este controlador no lo toca.</p>
 */
@Slf4j
@RestController
public class LoginRedirectController {

    private final String frontendLoginUri;

    public LoginRedirectController(@Value("${app.frontend.redirect-uri}") String frontendRedirectUri) {
        this.frontendLoginUri = deriveLoginUri(frontendRedirectUri);
    }

    @GetMapping("/login")
    public ResponseEntity<Void> loginFallback() {
        log.warn("GET /login alcanzado (probable fallo de callback OAuth2). Redirigiendo a {}", frontendLoginUri);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(frontendLoginUri))
                .build();
    }

    /** Deriva el origen (scheme://host[:port]) de la URI del frontend y apunta a /ingresar. */
    private static String deriveLoginUri(String frontendRedirectUri) {
        try {
            URI uri = URI.create(frontendRedirectUri);
            if (uri.getScheme() != null && uri.getAuthority() != null) {
                return uri.getScheme() + "://" + uri.getAuthority() + "/ingresar?error=oauth";
            }
        } catch (IllegalArgumentException ignored) {
            // cae al fallback
        }
        return "/ingresar?error=oauth";
    }
}
