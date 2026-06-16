package cl.duoc.siga.backend.security;

import cl.duoc.siga.backend.service.UsuarioService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Servicio de carga de usuario OIDC (Google). En cada login provisiona
 * (o recupera) el {@code Usuario} local asociado al correo de Google.
 */
@Service
public class SigaOidcUserService extends OidcUserService {

    private final UsuarioService usuarioService;

    public SigaOidcUserService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String correo = claim(oidcUser, "email");
        if (correo != null && !correo.isBlank()) {
            usuarioService.findOrCreateFromOAuth(correo, claim(oidcUser, "name"));
        }
        return oidcUser;
    }

    private static String claim(OidcUser user, String clave) {
        Object valor = user.getClaims().get(clave);
        return valor != null ? valor.toString() : null;
    }
}
