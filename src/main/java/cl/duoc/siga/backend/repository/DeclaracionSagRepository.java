package cl.duoc.siga.backend.repository;

import cl.duoc.siga.backend.model.DeclaracionSag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeclaracionSagRepository extends JpaRepository<DeclaracionSag, Long> {

    Optional<DeclaracionSag> findByTramiteId(Long tramiteId);

    boolean existsByTramiteId(Long tramiteId);
}
