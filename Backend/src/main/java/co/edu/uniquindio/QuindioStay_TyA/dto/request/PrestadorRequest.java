package co.edu.uniquindio.QuindioStay_TyA.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para registrar o editar un prestador. El estado no viaja aquí:
 * se cambia con las operaciones activar, suspender y retirar.
 */
public record PrestadorRequest(
        @NotBlank(message = "El nombre comercial es obligatorio")
        @Size(max = 120, message = "El nombre comercial admite máximo 120 caracteres")
        String nombreComercial,

        @NotBlank(message = "El NIT o documento es obligatorio")
        @Size(max = 20, message = "El NIT admite máximo 20 caracteres")
        String nitDocumento,

        @NotBlank(message = "El número de RNT es obligatorio")
        @Size(max = 30, message = "El RNT admite máximo 30 caracteres")
        String numeroRnt,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200, message = "La dirección admite máximo 200 caracteres")
        String direccion,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^3\\d{9}$", message = "El teléfono debe tener 10 dígitos y empezar por 3")
        String telefono,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 254, message = "El correo admite máximo 254 caracteres")
        String correo,

        @NotNull(message = "El municipio es obligatorio")
        Long idMunicipio,

        @NotNull(message = "El tipo de prestador es obligatorio")
        Long idTipoPrestador
) {
}
