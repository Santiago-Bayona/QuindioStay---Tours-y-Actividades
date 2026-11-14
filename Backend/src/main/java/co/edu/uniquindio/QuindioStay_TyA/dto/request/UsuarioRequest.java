package co.edu.uniquindio.QuindioStay_TyA.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o editar un usuario interno. La contraseña es obligatoria
 * al crear y opcional al editar. El prestador solo aplica al rol
 * "Encargado de prestador".
 */
public record UsuarioRequest(
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(max = 80, message = "El nombre de usuario admite máximo 80 caracteres")
        String nombreUsuario,

        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String contrasena,

        @NotNull(message = "El rol es obligatorio")
        Long idRol,

        Long idPrestador
) {
}
