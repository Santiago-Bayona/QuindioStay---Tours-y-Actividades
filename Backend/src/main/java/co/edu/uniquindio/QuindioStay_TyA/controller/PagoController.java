package co.edu.uniquindio.QuindioStay_TyA.controller;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPago;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.PagoRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.PagoResponse;
import co.edu.uniquindio.QuindioStay_TyA.service.AutorizacionService;
import co.edu.uniquindio.QuindioStay_TyA.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Pagos de las reservas (pago en línea simulado).
 * <p>
 * El service valida que el pago no exceda el saldo, que tarjeta y PSE traigan una
 * referencia única y que la reserva no esté cancelada. El primer pago exitoso
 * confirma la reserva. El historial de pagos nunca se borra.
 */
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;
    private final AutorizacionService autorizacion;

    /** {@code GET /api/pagos}: lista pagos; con {@code ?idReserva=} solo los de esa reserva. */
    @GetMapping
    public List<PagoResponse> listar(@RequestParam(required = false) Long idReserva) {
        if (idReserva != null) {
            return pagoService.listarPorReserva(idReserva);
        }
        return pagoService.listar();
    }

    /** {@code GET /api/pagos/{id}}: obtiene un pago. */
    @GetMapping("/{id}")
    public PagoResponse obtener(@PathVariable Long id) {
        return pagoService.obtener(id);
    }

    /** {@code POST /api/pagos}: registra un pago (abono, saldo o pago completo). Responde 201. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PagoResponse registrar(@Valid @RequestBody PagoRequest req) {
        return pagoService.registrar(req);
    }

    /**
     * {@code PATCH /api/pagos/{id}/resolver?estado=EXITOSO}: aprueba o rechaza un pago
     * PENDIENTE (simula la respuesta de la pasarela). Solo usuarios internos.
     */
    @PatchMapping("/{id}/resolver")
    public PagoResponse resolver(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                 @PathVariable Long id,
                                 @RequestParam EstadoPago estado) {
        autorizacion.exigirUsuarioInterno(idUsuario);
        return pagoService.resolverPendiente(id, estado);
    }
}
