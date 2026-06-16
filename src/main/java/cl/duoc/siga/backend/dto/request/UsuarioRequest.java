package cl.duoc.siga.backend.dto.request;

import cl.duoc.siga.backend.enums.RolFuncionario;
import cl.duoc.siga.backend.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @Size(max = 12, message = "El rut no puede superar 12 caracteres")
        String rut,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150)
        String correo,

        @NotNull(message = "El tipo de usuario es obligatorio")
        TipoUsuario tipoUsuario,

        RolFuncionario rolFuncionario
) {
}
