package cl.duoc.siga.backend.security;

import cl.duoc.siga.backend.model.Usuario;
import cl.duoc.siga.backend.service.UsuarioService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Servicio de carga de usuario OIDC (Google). En cada login provisiona (o recupera) el
 * {@code Usuario} local asociado al correo, y añade la autoridad {@code ROLE_<tipoUsuario>}
 * manteniendo las authorities OIDC originales (SCOPE_*). Así el rol se puede usar para
 * proteger rutas de forma uniforme con el login manual, sin romper el flujo de Google.
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
        if (correo == null || correo.isBlank()) {
            return oidcUser;
        }
        Usuario usuario = usuarioService.findOrCreateFromOAuth(correo, claim(oidcUser, "name"));

        Set<GrantedAuthority> authorities = new LinkedHashSet<>(oidcUser.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getTipoUsuario().name()));

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        if (userNameAttributeName == null || userNameAttributeName.isBlank()) {
            userNameAttributeName = "sub";
        }
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), userNameAttributeName);
    }

    private static String claim(OidcUser user, String clave) {
        Object valor = user.getClaims().get(clave);
        return valor != null ? valor.toString() : null;
    }
}
