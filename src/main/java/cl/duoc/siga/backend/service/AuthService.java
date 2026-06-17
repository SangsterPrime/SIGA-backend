package cl.duoc.siga.backend.service;

import cl.duoc.siga.backend.dto.request.LoginRequest;
import cl.duoc.siga.backend.dto.request.RegisterRequest;
import cl.duoc.siga.backend.enums.AuthProvider;
import cl.duoc.siga.backend.enums.EstadoCuenta;
import cl.duoc.siga.backend.enums.TipoUsuario;
import cl.duoc.siga.backend.exception.ConflictException;
import cl.duoc.siga.backend.model.Usuario;
import cl.duoc.siga.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Autenticación manual (email + contraseña), complementaria al login con Google.
 *
 * <p>Las contraseñas se almacenan solo como hash BCrypt en {@code usuario.password_hash}.
 * Los usuarios provisionados por Google tienen {@code passwordHash == null} y
 * {@code provider == GOOGLE}; los manuales quedan con {@code provider == LOCAL}.</p>
 *
 * <p>El rol se persiste en {@code tipo_usuario}. No se confía en el rol recibido:
 * ADMINISTRADOR no es auto-registrable y FUNCIONARIO exige un código de empleado válido,
 * leído de la variable de entorno {@code EMPLOYEE_REGISTER_CODE}.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /** Código requerido para registrar un FUNCIONARIO (env var, nunca en el frontend). */
    @Value("${app.auth.employee-register-code}")
    private String employeeRegisterCode;

    /** Crea un usuario manual con la contraseña hasheada y el rol validado. */
    public Usuario registrar(RegisterRequest req) {
        TipoUsuario role = req.role();
        if (role == TipoUsuario.ADMINISTRADOR) {
            throw new ConflictException("No se permite el registro con rol ADMINISTRADOR.");
        }
        if (role == TipoUsuario.FUNCIONARIO) {
            String code = req.employeeCode();
            if (code == null || code.isBlank() || !employeeRegisterCode.equals(code)) {
                throw new ConflictException("Código de funcionario inválido o ausente.");
            }
        }
        String correo = req.email().trim().toLowerCase();
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictException("Ya existe una cuenta con el correo: " + correo);
        }
        Usuario u = new Usuario();
        u.setNombre(req.name().trim());
        u.setCorreo(correo);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setTipoUsuario(role);
        u.setProvider(AuthProvider.LOCAL);
        u.setEstadoCuenta(EstadoCuenta.ACTIVA);
        return usuarioRepository.save(u);
    }

    /**
     * Verifica las credenciales. Lanza {@link BadCredentialsException} (→ 401) ante cualquier
     * fallo, sin revelar si el correo existe o si la contraseña es la incorrecta.
     */
    @Transactional(readOnly = true)
    public Usuario autenticar(LoginRequest req) {
        String correo = req.email().trim().toLowerCase();
        Usuario u = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new BadCredentialsException("Correo o contraseña incorrectos"));
        if (u.getPasswordHash() == null || !passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw new BadCredentialsException("Correo o contraseña incorrectos");
        }
        if (u.getEstadoCuenta() == EstadoCuenta.DESHABILITADA) {
            throw new BadCredentialsException("La cuenta está deshabilitada");
        }
        return u;
    }
}
