package cl.duoc.siga.backend.config;

import org.springframework.boot.web.server.servlet.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fuerza {@code SameSite=None} en todas las cookies del servidor embebido (incluida
 * {@code JSESSIONID}), de forma fiable en Spring Boot 4 / Tomcat 11.
 *
 * <p>Necesario para login cross-site: el frontend en Vercel
 * ({@code https://siga-fronted.vercel.app}) y el backend en Render
 * ({@code https://siga-backend-cs0t.onrender.com}) son sitios distintos. Para que el
 * navegador adjunte la cookie de sesión en una petición {@code fetch(..., { credentials:
 * "include" })} cross-site, la cookie debe viajar con {@code SameSite=None; Secure}.</p>
 *
 * <p>El binding por propiedad ({@code server.servlet.session.cookie.same-site=none}) no es
 * 100% fiable para {@code JSESSIONID} en Boot 4; este bean es el mecanismo soportado y
 * autoritativo. El atributo {@code Secure} (obligatorio cuando {@code SameSite=None}) lo
 * fija {@code server.servlet.session.cookie.secure=true} en application.properties.</p>
 */
@Configuration
public class CookieConfig {

    @Bean
    public CookieSameSiteSupplier cookieSameSiteSupplier() {
        return CookieSameSiteSupplier.ofNone();
    }
}
