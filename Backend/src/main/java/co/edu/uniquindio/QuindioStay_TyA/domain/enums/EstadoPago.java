package co.edu.uniquindio.QuindioStay_TyA.domain.enums;

/**
 * Estados de un pago.
 */
public enum EstadoPago {
    /** El dinero fue recibido; cuenta para saber si la reserva está pagada. */
    EXITOSO,
    /** El pago fue rechazado. */
    FALLIDO,
    /** El pago está en proceso. */
    PENDIENTE,
    /** Registro de un dinero devuelto al cliente. */
    REEMBOLSADO
}
