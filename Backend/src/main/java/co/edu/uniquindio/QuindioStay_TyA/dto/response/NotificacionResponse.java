package co.edu.uniquindio.QuindioStay_TyA.dto.response;

import co.edu.uniquindio.QuindioStay_TyA.domain.Notificacion;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.CanalNotificacion;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoNotificacion;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.TipoNotificacion;

import java.time.LocalDateTime;

/**
 * Notificación devuelta por la API.
 */
public record NotificacionResponse(
        Long idNotificacion,
        TipoNotificacion tipo,
        CanalNotificacion canal,
        String mensaje,
        LocalDateTime fechaProgramada,
        LocalDateTime fechaEnvio,
        EstadoNotificacion estado,
        Long idCliente,
        Long idReserva
) {

    public static NotificacionResponse de(Notificacion n) {
        return new NotificacionResponse(n.getIdNotificacion(), n.getTipo(), n.getCanal(), n.getMensaje(),
                n.getFechaProgramada(), n.getFechaEnvio(), n.getEstado(),
                n.getCliente().getIdCliente(),
                n.getReserva() == null ? null : n.getReserva().getIdReserva());
    }
}
