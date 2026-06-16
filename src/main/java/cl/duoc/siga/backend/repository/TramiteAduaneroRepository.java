package cl.duoc.siga.backend.repository;

import cl.duoc.siga.backend.enums.EstadoTramite;
import cl.duoc.siga.backend.model.TramiteAduanero;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TramiteAduaneroRepository extends JpaRepository<TramiteAduanero, Long> {

    Optional<TramiteAduanero> findByFolio(String folio);

    List<TramiteAduanero> findByPasajeroId(Long pasajeroId);

    List<TramiteAduanero> findByEstado(EstadoTramite estado);

    boolean existsByFolio(String folio);
}
