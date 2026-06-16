package cl.duoc.siga.backend.exception;

import java.time.OffsetDateTime;
import java.util.Map;

/** Cuerpo estándar de respuesta de error de la API. */
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, message, path, null);
    }

    public static ErrorResponse of(int status, String error, String message, String path,
                                   Map<String, String> fieldErrors) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, message, path, fieldErrors);
    }
}
