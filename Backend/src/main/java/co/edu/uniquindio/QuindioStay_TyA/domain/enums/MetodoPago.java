package co.edu.uniquindio.QuindioStay_TyA.domain.enums;

/**
 * Métodos con los que un cliente puede pagar una reserva.
 */
public enum MetodoPago {
    /** Tarjeta de crédito (pago en línea). */
    TARJETA_CREDITO,
    /** Tarjeta débito (pago en línea). */
    TARJETA_DEBITO,
    /** PSE (pago en línea). */
    PSE,
    /** Transferencia bancaria. */
    TRANSFERENCIA,
    /** Efectivo en el mostrador. */
    EFECTIVO;

    /**
     * Indica si el método es un pago en línea. Los pagos en línea siempre
     * generan una referencia de transacción, por lo que esta es obligatoria.
     *
     * @return {@code true} para tarjeta de crédito, tarjeta débito y PSE
     */
    public boolean esEnLinea() {
        return this == TARJETA_CREDITO || this == TARJETA_DEBITO || this == PSE;
    }
}
