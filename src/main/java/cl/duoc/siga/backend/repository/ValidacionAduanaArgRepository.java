package cl.duoc.siga.backend.repository;

import cl.duoc.siga.backend.model.ValidacionAduanaArg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ValidacionAduanaArgRepository extends JpaRepository<ValidacionAduanaArg, Long> {

    Optional<ValidacionAduanaArg> findByTramiteId(Long tramiteId);
}
