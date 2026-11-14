package co.edu.uniquindio.QuindioStay_TyA.dto.response;

import co.edu.uniquindio.QuindioStay_TyA.domain.Participante;
import co.edu.uniquindio.QuindioStay_TyA.domain.Reserva;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.CanalReserva;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Reserva devuelta por la API, con sus participantes y el estado de pago.
 */
public record ReservaResponse(
        Long idReserva,
        LocalDateTime fechaCreacion,
        LocalDate fechaServicio,
        EstadoReserva estado,
        BigDecimal valorTotal,
        LocalDateTime fechaCancelacion,
        CanalReserva canal,
        Long idCliente,
        String cliente,
        Long idPlan,
        String plan,
        Long idSalida,
        String tour,
        List<ParticipanteResponse> participantes,
        BigDecimal totalPagado,
        BigDecimal saldoPendiente
) {

    public static ReservaResponse de(Reserva r) {
        List<ParticipanteResponse> participantes = r.getParticipantes().stream()
                .sorted(Comparator.comparing(Participante::getNumParticipante))
                .map(ParticipanteResponse::de)
                .toList();
        return new ReservaResponse(
                r.getIdReserva(), r.getFechaCreacion(), r.getFechaServicio(), r.getEstado(), r.getValorTotal(),
                r.getFechaCancelacion(), r.getCanal(),
                r.getCliente().getIdCliente(), r.getCliente().nombreCompleto(),
                r.getPlan().getIdPlan(), r.getPlan().getNombre(),
                r.getSalida().getIdSalida(), r.getSalida().getTour().getNombre(),
                participantes, r.totalPagadoExitoso(), r.saldoPendiente());
    }
}
