package co.edu.uniquindio.QuindioStay_TyA.dto.response;

import co.edu.uniquindio.QuindioStay_TyA.domain.Pago;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPago;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pago devuelto por la API.
 */
public record PagoResponse(
        Long idPago,
        LocalDateTime fecha,
        BigDecimal monto,
        MetodoPago metodo,
        EstadoPago estado,
        String referenciaTransaccion,
        Long idReserva
) {

    public static PagoResponse de(Pago p) {
        return new PagoResponse(p.getIdPago(), p.getFecha(), p.getMonto(), p.getMetodo(), p.getEstado(),
                p.getReferenciaTransaccion(), p.getReserva().getIdReserva());
    }
}
