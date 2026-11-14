package co.edu.uniquindio.QuindioStay_TyA.dto.request;

/**
 * Indica quién cancela la reserva, porque de eso depende el reembolso.
 *
 * @param porOperador {@code true} si cancela el operador (reembolso del 100 %);
 *                    {@code false} si cancela el cliente (80 % si faltan más de 5 días)
 */
public record CancelarReservaRequest(boolean porOperador) {
}
