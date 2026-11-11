package co.edu.uniquindio.QuindioStay_TyA.domain.enums;

/**
 * Estados posibles de un prestador. Un prestador nunca se borra: se retira.
 */
public enum EstadoPrestador {
    /** Puede publicar tours y programar salidas. */
    ACTIVO,
    /** Temporalmente inhabilitado; conserva su historial. */
    SUSPENDIDO,
    /** Salió de la plataforma; su historial se conserva. */
    RETIRADO
}
