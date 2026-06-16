package cl.duoc.siga.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/** Tras un login exitoso con Google, redirige al frontend configurado. */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public OAuth2LoginSuccessHandler(@Value("${app.frontend.redirect-uri}") String redirectUri) {
        setDefaultTargetUrl(redirectUri);
        setAlwaysUseDefaultTargetUrl(true);
    }
}
