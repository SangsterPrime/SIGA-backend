package cl.duoc.siga.backend.repository;

import cl.duoc.siga.backend.model.RevisionPdi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RevisionPdiRepository extends JpaRepository<RevisionPdi, Long> {

    Optional<RevisionPdi> findByTramiteId(Long tramiteId);

    boolean existsByTramiteId(Long tramiteId);
}
