package cl.duoc.siga.backend.repository;

import cl.duoc.siga.backend.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByTramiteId(Long tramiteId);
}
