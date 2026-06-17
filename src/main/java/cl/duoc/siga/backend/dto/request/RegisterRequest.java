package cl.duoc.siga.backend.dto.request;

import cl.duoc.siga.backend.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Registro manual. El {@code role} NO se confía tal cual: se valida en el servicio
 * (ADMINISTRADOR no es auto-registrable; FUNCIONARIO exige {@code employeeCode} correcto).
 */
public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String name,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres")
        String email,

        // BCrypt trunca a 72 bytes; limitamos arriba para evitar sorpresas.
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        TipoUsuario role,

        String employeeCode
) {
}
