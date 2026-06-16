package cl.duoc.siga.backend.repository;

import cl.duoc.siga.backend.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    Optional<Vehiculo> findByTramiteId(Long tramiteId);

    boolean existsByTramiteId(Long tramiteId);
}
