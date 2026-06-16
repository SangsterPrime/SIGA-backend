package cl.duoc.siga.backend.exception;

/** Se lanza ante un conflicto de negocio (p. ej. recurso duplicado). Se traduce a HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String mensaje) {
        super(mensaje);
    }
}
