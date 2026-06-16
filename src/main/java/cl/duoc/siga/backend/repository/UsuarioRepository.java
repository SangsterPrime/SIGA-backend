package cl.duoc.siga.backend.repository;

import cl.duoc.siga.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByRut(String rut);

    boolean existsByCorreo(String correo);

    boolean existsByRut(String rut);
}
