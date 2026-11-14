package co.edu.uniquindio.QuindioStay_TyA.dto.request;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.CanalReserva;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Datos para crear una reserva: un cliente reserva un plan en una salida para
 * una o más personas. La fecha del servicio es la de la salida y el valor se
 * calcula en el servidor (precio por persona del tour x participantes).
 */
public record ReservaRequest(
        @NotNull(message = "El cliente es obligatorio")
        Long idCliente,

        @NotNull(message = "El plan es obligatorio")
        Long idPlan,

        @NotNull(message = "La salida es obligatoria")
        Long idSalida,

        @NotNull(message = "El canal de la reserva es obligatorio")
        CanalReserva canal,

        @NotEmpty(message = "La reserva debe tener al menos un participante")
        @Valid
        List<ParticipanteRequest> participantes
) {
}
