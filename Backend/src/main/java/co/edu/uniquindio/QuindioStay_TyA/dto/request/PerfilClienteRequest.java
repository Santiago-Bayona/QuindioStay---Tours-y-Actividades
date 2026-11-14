package co.edu.uniquindio.QuindioStay_TyA.dto.request;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.CanalNotificacion;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.IdiomaPreferencia;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos de la cuenta y las preferencias de un cliente. La contraseña es
 * obligatoria al crear el perfil y opcional al editarlo.
 */
public record PerfilClienteRequest(
        @NotBlank(message = "El correo de la cuenta es obligatorio")
        @Email(message = "El correo de la cuenta no tiene un formato válido")
        @Size(max = 254, message = "El correo admite máximo 254 caracteres")
        String correoCuenta,

        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String contrasena,

        @NotNull(message = "El idioma de preferencia es obligatorio")
        IdiomaPreferencia idiomaPreferencia,

        @NotNull(message = "El canal de notificación es obligatorio")
        CanalNotificacion canalNotificacionPreferido,

        @NotNull(message = "La anticipación de los recordatorios es obligatoria (1, 3 o 7 días)")
        Integer anticipacionRecordatorios,

        @NotNull(message = "Indique si acepta promociones")
        Boolean aceptaPromociones
) {
}
