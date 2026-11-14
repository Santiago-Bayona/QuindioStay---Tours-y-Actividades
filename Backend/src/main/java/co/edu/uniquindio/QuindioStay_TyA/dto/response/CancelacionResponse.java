package co.edu.uniquindio.QuindioStay_TyA.dto.response;

import java.math.BigDecimal;

/**
 * Resultado de cancelar una reserva.
 *
 * @param reserva   reserva ya cancelada
 * @param reembolso monto que se devolvió al cliente (0 si no corresponde)
 */
public record CancelacionResponse(ReservaResponse reserva, BigDecimal reembolso) {
}
