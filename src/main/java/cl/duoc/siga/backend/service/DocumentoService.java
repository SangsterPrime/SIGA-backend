package cl.duoc.siga.backend.service;

import cl.duoc.siga.backend.dto.request.DocumentoRequest;
import cl.duoc.siga.backend.dto.response.DocumentoResponse;
import cl.duoc.siga.backend.exception.ResourceNotFoundException;
import cl.duoc.siga.backend.mapper.DocumentoMapper;
import cl.duoc.siga.backend.model.Documento;
import cl.duoc.siga.backend.repository.DocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final TramiteService tramiteService;

    @Transactional(readOnly = true)
    public List<DocumentoResponse> listarPorTramite(Long tramiteId) {
        return documentoRepository.findByTramiteId(tramiteId)
                .stream().map(DocumentoMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DocumentoResponse obtener(Long id) {
        return DocumentoMapper.toResponse(obtenerEntidad(id));
    }

    public DocumentoResponse crear(DocumentoRequest req) {
        Documento d = new Documento();
        d.setTramite(tramiteService.obtenerEntidad(req.tramiteId()));
        d.setTipoDocumento(req.tipoDocumento());
        d.setNumeroDocumento(req.numeroDocumento());
        d.setUrlArchivo(req.urlArchivo());
        return DocumentoMapper.toResponse(documentoRepository.save(d));
    }

    public void eliminar(Long id) {
        documentoRepository.delete(obtenerEntidad(id));
    }

    private Documento obtenerEntidad(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", id));
    }
}
