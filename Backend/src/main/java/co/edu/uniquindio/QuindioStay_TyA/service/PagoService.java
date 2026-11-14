package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.Pago;
import co.edu.uniquindio.QuindioStay_TyA.domain.Reserva;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPago;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoReserva;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.TipoNotificacion;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.PagoRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.PagoResponse;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.exception.ReglaNegocioException;
import co.edu.uniquindio.QuindioStay_TyA.repository.PagoRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Pagos de las reservas.
 *
 *  Una reserva se puede pagar en una o varias cuotas; un pago exitoso no puede superar el saldo pendiente.
 *  Los pagos con tarjeta o PSE exigen referencia de transacción, que no se repite.
 *  El primer pago exitoso confirma la reserva pendiente y genera la notificación de pago.
 *  Los pagos no se borran. Los reembolsos los crea el sistema al cancelar la reserva.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaService reservaService;
    private final NotificacionService notificacionService;

    @Transactional(readOnly = true)
    public List<PagoResponse> listar() {
        return pagoRepository.findAll(Sort.by(Sort.Direction.DESC, "fecha")).stream().map(PagoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorReserva(Long idReserva) {
        return pagoRepository.listarPorReserva(idReserva).stream().map(PagoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PagoResponse obtener(Long id) {
        return PagoResponse.de(buscar(id));
    }

    public PagoResponse registrar(PagoRequest req) {
        Reserva reserva = reservaRepository.findById(req.idReserva())
                .orElseThrow(() -> new RecursoNoEncontradoException("la reserva", req.idReserva()));
        if (reserva.getEstado() == EstadoReserva.CANCELADA || reserva.getEstado() == EstadoReserva.COMPLETADA) {
            throw new ReglaNegocioException("No se registran pagos en una reserva "
                    + reserva.getEstado().name().toLowerCase());
        }
        EstadoPago estado = req.estado() == null ? EstadoPago.EXITOSO : req.estado();
        if (estado == EstadoPago.REEMBOLSADO) {
            throw new ReglaNegocioException("Los reembolsos los genera el sistema al cancelar la reserva");
        }
        String referencia = req.referenciaTransaccion() == null || req.referenciaTransaccion().isBlank()
                ? null : req.referenciaTransaccion().trim();

        Pago pago = new Pago(req.monto(), req.metodo(), referencia);
        pago.setEstado(estado);
        pago.validarReferencia();
        if (referencia != null && pagoRepository.buscarPorReferencia(referencia).isPresent()) {
            throw new ReglaNegocioException("Ya existe un pago con esa referencia de transacción");
        }
        if (estado == EstadoPago.EXITOSO) {
            validarNoExcedeSaldo(reserva, pago.getMonto());
        }

        reserva.agregarPago(pago);
        pagoRepository.saveAndFlush(pago);
        if (estado == EstadoPago.EXITOSO) {
            alAprobarse(reserva, pago);
        }
        return PagoResponse.de(pago);
    }

    /** Resuelve un pago pendiente: pasa a EXITOSO o a FALLIDO. */
    public PagoResponse resolverPendiente(Long idPago, EstadoPago nuevoEstado) {
        Pago pago = buscar(idPago);
        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new ReglaNegocioException("Solo se puede resolver un pago pendiente");
        }
        Reserva reserva = pago.getReserva();
        if (nuevoEstado == EstadoPago.EXITOSO) {
            if (reserva.getEstado() == EstadoReserva.CANCELADA) {
                throw new ReglaNegocioException("La reserva está cancelada: no se puede aprobar el pago");
            }
            validarNoExcedeSaldo(reserva, pago.getMonto());
            pago.marcarExitoso();
            pagoRepository.saveAndFlush(pago);
            alAprobarse(reserva, pago);
        } else if (nuevoEstado == EstadoPago.FALLIDO) {
            pago.marcarFallido();
            pagoRepository.saveAndFlush(pago);
        } else {
            throw new ReglaNegocioException("Un pago pendiente solo puede pasar a EXITOSO o FALLIDO");
        }
        return PagoResponse.de(pago);
    }

    private Pago buscar(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el pago", id));
    }

    private void validarNoExcedeSaldo(Reserva reserva, BigDecimal monto) {
        BigDecimal saldo = reserva.saldoPendiente();
        if (monto.compareTo(saldo) > 0) {
            throw new ReglaNegocioException("El pago de $" + monto.toPlainString()
                    + " supera el saldo pendiente de la reserva ($" + saldo.toPlainString() + ")");
        }
    }

    private void alAprobarse(Reserva reserva, Pago pago) {
        String mensaje = "Recibimos tu pago de $" + pago.getMonto().toPlainString()
                + " de la reserva " + reserva.getIdReserva() + ".";
        notificacionService.notificarEvento(reserva.getCliente(), reserva,
                TipoNotificacion.CONFIRMACION_PAGO, mensaje, LocalDateTime.now());
        reservaService.confirmarPorPago(reserva);
    }
}
