package co.edu.uniquindio.QuindioStay_TyA.domain.enums;

/**
 * Tipos de notificación que envía la plataforma.
 */
public enum TipoNotificacion {
    /** Recordatorio previo al servicio; siempre ligado a una reserva. */
    RECORDATORIO_RESERVA,
    /** Confirmación de una reserva. */
    CONFIRMACION_RESERVA,
    /** Confirmación (o fallo) de un pago. */
    CONFIRMACION_PAGO,
    /** Aviso de cambio de estado, por ejemplo una salida cancelada. */
    CAMBIO_ESTADO,
    /** Promoción; no se liga a ninguna reserva y solo se envía a quien la acepta. */
    PROMOCION
}
