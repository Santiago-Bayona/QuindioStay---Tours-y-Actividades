package co.edu.uniquindio.QuindioStay_TyA.dto.request;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.CanalNotificacion;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.TipoNotificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Datos para programar una notificación. Una promoción no lleva reserva y un
 * recordatorio sí.
 */
public record NotificacionRequest(
        @NotNull(message = "El tipo es obligatorio")
        TipoNotificacion tipo,

        @NotNull(message = "El canal es obligatorio")
        CanalNotificacion canal,

        @NotBlank(message = "El mensaje es obligatorio")
        String mensaje,

        @NotNull(message = "La fecha programada es obligatoria")
        LocalDateTime fechaProgramada,

        @NotNull(message = "El cliente es obligatorio")
        Long idCliente,

        Long idReserva
) {
}
