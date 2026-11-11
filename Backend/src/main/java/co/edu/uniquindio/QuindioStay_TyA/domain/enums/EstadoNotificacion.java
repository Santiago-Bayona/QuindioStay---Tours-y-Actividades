package co.edu.uniquindio.QuindioStay_TyA.domain.enums;

/**
 * Estados de una notificación.
 */
public enum EstadoNotificacion {
    /** Programada, aún no enviada. */
    PENDIENTE,
    /** Enviada al cliente. */
    ENVIADA,
    /** No se pudo enviar. */
    FALLIDA,
    /** El cliente la leyó. */
    LEIDA
}
