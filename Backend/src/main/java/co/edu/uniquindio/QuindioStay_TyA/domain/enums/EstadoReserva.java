package co.edu.uniquindio.QuindioStay_TyA.domain.enums;

/**
 * Estados del ciclo de vida de una reserva.
 */
public enum EstadoReserva {
    /** Creada, aún sin confirmar. */
    PENDIENTE,
    /** Confirmada; puede completarse. */
    CONFIRMADA,
    /** Cancelada por el cliente o por el operador; queda como historial. */
    CANCELADA,
    /** El servicio ya se prestó. Solo se llega desde CONFIRMADA. */
    COMPLETADA
}
