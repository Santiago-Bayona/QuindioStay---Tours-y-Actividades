package co.edu.uniquindio.QuindioStay_TyA.dto.request;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.TipoDocumento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Datos para registrar o editar un cliente. El perfil es opcional: un cliente
 * atendido en el mostrador puede no tener cuenta.
 */
public record ClienteRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre admite máximo 80 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 80, message = "El apellido admite máximo 80 caracteres")
        String apellido,

        @NotNull(message = "El tipo de documento es obligatorio")
        TipoDocumento tipoDocumento,

        @NotBlank(message = "El número de documento es obligatorio")
        @Size(max = 30, message = "El documento admite máximo 30 caracteres")
        String numeroDocumento,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 254, message = "El correo admite máximo 254 caracteres")
        String correo,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^3\\d{9}$", message = "El teléfono debe tener 10 dígitos y empezar por 3")
        String telefono,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate fechaNacimiento,

        @NotBlank(message = "La ciudad de origen es obligatoria")
        @Size(max = 100, message = "La ciudad admite máximo 100 caracteres")
        String ciudadOrigen,

        @Valid
        PerfilClienteRequest perfil
) {
}
