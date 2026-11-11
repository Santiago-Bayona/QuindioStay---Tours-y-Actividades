package co.edu.uniquindio.QuindioStay_TyA.domain.enums;

/**
 * Estados de un plan. Un plan vendido no se borra: se inactiva.
 */
public enum EstadoPlan {
    /** Disponible para nuevas reservas. */
    ACTIVO,
    /** No acepta nuevas reservas; conserva su historial. */
    INACTIVO
}
