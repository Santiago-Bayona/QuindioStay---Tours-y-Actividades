package co.edu.uniquindio.QuindioStay_TyA.dto.request;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPago;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Datos para registrar un pago de una reserva. Si no se envía el estado se
 * asume EXITOSO. Los reembolsos no se registran aquí: los genera el sistema.
 */
public record PagoRequest(
        @NotNull(message = "La reserva es obligatoria")
        Long idReserva,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.00", inclusive = false, message = "El monto debe ser mayor que cero")
        @Digits(integer = 10, fraction = 2, message = "El monto admite máximo 2 decimales")
        BigDecimal monto,

        @NotNull(message = "El método de pago es obligatorio")
        MetodoPago metodo,

        @Size(max = 100, message = "La referencia admite máximo 100 caracteres")
        String referenciaTransaccion,

        EstadoPago estado
) {
}
