package co.edu.uniquindio.QuindioStay_TyA.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cambio de contraseña de un usuario interno.
 */
public record CambiarContrasenaRequest(
        @NotBlank(message = "La contraseña actual es obligatoria")
        String actual,

        @NotBlank(message = "La contraseña nueva es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String nueva
) {
}
