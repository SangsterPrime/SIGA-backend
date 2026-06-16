package cl.duoc.siga.backend.controller;

import cl.duoc.siga.backend.dto.response.UsuarioResponse;
import cl.duoc.siga.backend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint protegido que devuelve el usuario autenticado vía Google OAuth2. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(@AuthenticationPrincipal OidcUser principal) {
        Object correo = principal != null ? principal.getClaims().get("email") : null;
        if (correo == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(usuarioService.obtenerPorCorreo(correo.toString()));
    }
}
