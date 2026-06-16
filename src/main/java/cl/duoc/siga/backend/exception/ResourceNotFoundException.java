package cl.duoc.siga.backend.exception;

/** Se lanza cuando no se encuentra un recurso solicitado. Se traduce a HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }

    public ResourceNotFoundException(String recurso, Object id) {
        super(recurso + " no encontrado con id: " + id);
    }
}
