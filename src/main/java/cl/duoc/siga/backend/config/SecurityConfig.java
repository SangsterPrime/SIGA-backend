package cl.duoc.siga.backend.config;

import cl.duoc.siga.backend.security.OAuth2LoginSuccessHandler;
import cl.duoc.siga.backend.security.SigaOidcUserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configuración de seguridad.
 *
 * <p>Endpoints públicos: raíz, /error, /api/public/**, rutas de OAuth2 y health de actuator.
 * El resto requiere autenticación (login con Google).</p>
 *
 * <p>El login con Google solo se monta si existe un {@code ClientRegistrationRepository}
 * (es decir, si GOOGLE_CLIENT_ID está configurado). Así la aplicación arranca igual sin
 * credenciales de Google.</p>
 *
 * <p>CSRF se deshabilita para simplificar el consumo de la API REST desde el frontend / curl
 * en este MVP académico; en producción se habilitaría con tokens.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SigaOidcUserService oidcUserService;
    private final OAuth2LoginSuccessHandler successHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(SigaOidcUserService oidcUserService,
                          OAuth2LoginSuccessHandler successHandler,
                          CorsConfigurationSource corsConfigurationSource) {
        this.oidcUserService = oidcUserService;
        this.successHandler = successHandler;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/error", "/api/public/**",
                                "/auth/**",
                                "/oauth2/**", "/login/**",
                                "/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        // Solo se activa el login con Google si hay credenciales OAuth2 configuradas.
        if (clientRegistrationRepository.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth
                    .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService))
                    .successHandler(successHandler));
        }

        return http.build();
    }

    /**
     * Codificador de contraseñas para el registro/login manual. BCrypt viene incluido en
     * {@code spring-security-crypto} (sin dependencias extra). Los usuarios de Google no
     * usan {@code passwordHash}; los manuales lo tienen obligatorio y hasheado.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
