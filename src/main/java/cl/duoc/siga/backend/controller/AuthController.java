package cl.duoc.siga.backend.controller;

import cl.duoc.siga.backend.dto.response.UsuarioResponse;
import cl.duoc.siga.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint protegido que devuelve el usuario autenticado vía Google OAuth2. */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(@AuthenticationPrincipal OidcUser principal) {
        // Logs temporales de diagnóstico (sin secretos).
        if (principal == null) {
            log.warn("/api/me -> principal NULL: no llegó sesión válida (revisar cookie JSESSIONID cross-site).");
            return ResponseEntity.status(401).build();
        }
        Object correo = principal.getClaims().get("email");
        log.info("/api/me -> authenticated=true, email={}", correo);
        if (correo == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(usuarioService.obtenerPorCorreo(correo.toString()));
    }
}
