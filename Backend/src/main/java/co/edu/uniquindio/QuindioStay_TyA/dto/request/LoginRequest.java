package co.edu.uniquindio.QuindioStay_TyA.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales de acceso. Para usuarios internos {@code usuario} es el nombre
 * de usuario; para clientes es el correo de la cuenta.
 */
public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio")
        String usuario,

        @NotBlank(message = "La contraseña es obligatoria")
        String contrasena
) {
}
