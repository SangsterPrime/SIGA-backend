package cl.duoc.siga.backend.service;

import cl.duoc.siga.backend.dto.request.UsuarioRequest;
import cl.duoc.siga.backend.dto.response.UsuarioResponse;
import cl.duoc.siga.backend.enums.AuthProvider;
import cl.duoc.siga.backend.enums.EstadoCuenta;
import cl.duoc.siga.backend.enums.TipoUsuario;
import cl.duoc.siga.backend.exception.ConflictException;
import cl.duoc.siga.backend.exception.ResourceNotFoundException;
import cl.duoc.siga.backend.mapper.UsuarioMapper;
import cl.duoc.siga.backend.model.Usuario;
import cl.duoc.siga.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioMapper.toResponse(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public Usuario obtenerEntidad(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .map(UsuarioMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correo));
    }

    public UsuarioResponse crear(UsuarioRequest req) {
        validarRol(req);
        if (usuarioRepository.existsByCorreo(req.correo())) {
            throw new ConflictException("Ya existe un usuario con el correo: " + req.correo());
        }
        if (req.rut() != null && !req.rut().isBlank() && usuarioRepository.existsByRut(req.rut())) {
            throw new ConflictException("Ya existe un usuario con el rut: " + req.rut());
        }
        Usuario u = new Usuario();
        u.setRut(req.rut());
        u.setNombre(req.nombre());
        u.setCorreo(req.correo());
        u.setTipoUsuario(req.tipoUsuario());
        u.setRolFuncionario(req.rolFuncionario());
        u.setEstadoCuenta(EstadoCuenta.ACTIVA);
        return UsuarioMapper.toResponse(usuarioRepository.save(u));
    }

    public UsuarioResponse actualizar(Long id, UsuarioRequest req) {
        validarRol(req);
        Usuario u = obtenerEntidad(id);
        if (!u.getCorreo().equals(req.correo()) && usuarioRepository.existsByCorreo(req.correo())) {
            throw new ConflictException("Ya existe un usuario con el correo: " + req.correo());
        }
        u.setRut(req.rut());
        u.setNombre(req.nombre());
        u.setCorreo(req.correo());
        u.setTipoUsuario(req.tipoUsuario());
        u.setRolFuncionario(req.rolFuncionario());
        return UsuarioMapper.toResponse(u);
    }

    public void eliminar(Long id) {
        Usuario u = obtenerEntidad(id);
        usuarioRepository.delete(u);
    }

    /** Provisiona (o recupera) el usuario asociado a un login con Google. */
    public Usuario findOrCreateFromOAuth(String correo, String nombre) {
        return usuarioRepository.findByCorreo(correo).orElseGet(() -> {
            Usuario u = new Usuario();
            u.setCorreo(correo);
            u.setNombre(nombre != null && !nombre.isBlank() ? nombre : correo);
            u.setTipoUsuario(TipoUsuario.PASAJERO);
            u.setProvider(AuthProvider.GOOGLE);
            u.setEstadoCuenta(EstadoCuenta.ACTIVA);
            return usuarioRepository.save(u);
        });
    }

    /** Regla de dominio: solo los FUNCIONARIO pueden tener rol_funcionario. */
    private void validarRol(UsuarioRequest req) {
        if (req.tipoUsuario() != TipoUsuario.FUNCIONARIO && req.rolFuncionario() != null) {
            throw new ConflictException("Solo los usuarios de tipo FUNCIONARIO pueden tener un rol de funcionario");
        }
    }
}
