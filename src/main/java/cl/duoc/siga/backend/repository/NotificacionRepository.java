package cl.duoc.siga.backend.repository;

import cl.duoc.siga.backend.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByTramiteId(Long tramiteId);

    List<Notificacion> findByUsuarioDestinoId(Long usuarioDestinoId);
}
