package cl.duoc.siga.backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Tras un login exitoso con Google, redirige al frontend configurado. */
@Slf4j
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final String redirectUri;

    public OAuth2LoginSuccessHandler(@Value("${app.frontend.redirect-uri}") String redirectUri) {
        this.redirectUri = redirectUri;
        setDefaultTargetUrl(redirectUri);
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        // Logs temporales de diagnóstico (sin secretos: solo email/name del principal).
        String email = null;
        String name = null;
        if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidc) {
            email = String.valueOf(oidc.getClaims().get("email"));
            name = String.valueOf(oidc.getClaims().get("name"));
        }
        HttpSession session = request.getSession(false);
        log.info("OAuth2 login OK -> email={}, name={}, authenticated={}, sessionId={}, redirect={}",
                email, name,
                authentication != null && authentication.isAuthenticated(),
                session != null ? session.getId() : "NO-SESSION",
                redirectUri);

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
