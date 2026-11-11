package co.edu.uniquindio.QuindioStay_TyA.domain.enums;

/**
 * Estados de una salida programada de un tour.
 */
public enum EstadoSalida {
    /** Vigente; acepta reservas si su fecha es futura. */
    PROGRAMADA,
    /** Cancelada por el operador; sus reservas se cancelan con reembolso del 100 %. */
    CANCELADA,
    /** La salida ya se llevó a cabo. */
    REALIZADA
}
