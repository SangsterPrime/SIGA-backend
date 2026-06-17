package cl.duoc.siga.backend.controller;

import cl.duoc.siga.backend.dto.request.LoginRequest;
import cl.duoc.siga.backend.dto.request.RegisterRequest;
import cl.duoc.siga.backend.dto.response.AuthResponse;
import cl.duoc.siga.backend.mapper.UsuarioMapper;
import cl.duoc.siga.backend.model.Usuario;
import cl.duoc.siga.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Autenticación manual (email + contraseña). Tras un registro o login exitoso crea la
 * MISMA sesión de Spring Security que el login con Google (cookie {@code JSESSIONID}), de
 * modo que {@code GET /api/me} y el resto de endpoints protegidos funcionan igual para ambos
 * métodos. Convive con el login OAuth2 sin alterarlo.
 *
 * <p>Devuelve {@link AuthResponse} unificado: {@code token} (id de sesión, informativo) y
 * {@code user} sin {@code passwordHash}. La autoridad del rol se deriva de
 * {@code tipo_usuario} ({@code ROLE_PASAJERO} / {@code ROLE_FUNCIONARIO}).</p>
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class ManualAuthController {

    private final AuthService authService;

    // Persiste el SecurityContext en la HttpSession (misma cookie que usa OAuth2).
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();
    // Rota el id de sesión al autenticar para prevenir session fixation.
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy =
            new ChangeSessionIdAuthenticationStrategy();

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) {
        Usuario usuario = authService.registrar(req);
        String token = iniciarSesion(usuario, request, response);
        log.info("Registro manual OK -> correo={}, role={}", usuario.getCorreo(), usuario.getTipoUsuario());
        return ResponseEntity.ok(new AuthResponse(token, UsuarioMapper.toAuthUser(usuario)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        Usuario usuario = authService.autenticar(req);
        String token = iniciarSesion(usuario, request, response);
        log.info("Login manual OK -> correo={}, role={}", usuario.getCorreo(), usuario.getTipoUsuario());
        return ResponseEntity.ok(new AuthResponse(token, UsuarioMapper.toAuthUser(usuario)));
    }

    /**
     * Autentica la sesión actual con el correo como principal y la autoridad del rol, y la
     * guarda en la cookie de sesión. Devuelve el id de sesión como "token" informativo.
     */
    private String iniciarSesion(Usuario usuario, HttpServletRequest request, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                usuario.getCorreo(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getTipoUsuario().name())));
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        // Prevención de session fixation: si había sesión anónima previa, rota su id.
        sessionAuthenticationStrategy.onAuthentication(authentication, request, response);
        securityContextRepository.saveContext(context, request, response);
        return request.getSession().getId();
    }
}
