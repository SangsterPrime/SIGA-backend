package cl.duoc.siga.backend.repository;

import cl.duoc.siga.backend.model.HistorialEstadoTramite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoTramiteRepository extends JpaRepository<HistorialEstadoTramite, Long> {

    List<HistorialEstadoTramite> findByTramiteIdOrderByFechaCambioAsc(Long tramiteId);
}
