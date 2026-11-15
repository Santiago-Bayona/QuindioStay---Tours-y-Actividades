package co.edu.uniquindio.QuindioStay_TyA.controller;

import co.edu.uniquindio.QuindioStay_TyA.dto.request.CancelarReservaRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.ParticipanteRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.ReservaRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.CancelacionResponse;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.ReservaResponse;
import co.edu.uniquindio.QuindioStay_TyA.service.AutorizacionService;
import co.edu.uniquindio.QuindioStay_TyA.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Reservas de salidas y sus participantes.
 * <p>
 * Al crear, el service valida cupo, edad mínima, salida vigente, plan del mismo
 * prestador y vigente, y que ningún participante esté repetido; el valor total lo
 * calcula el backend (el cliente no lo envía). Una reserva nunca se borra: se cancela.
 */
@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;
    private final AutorizacionService autorizacion;

    /**
     * {@code GET /api/reservas}: lista reservas. Filtros opcionales (se usa el primero que llegue):
     * {@code ?idCliente=} (historial del cliente), {@code ?idSalida=} o {@code ?idPrestador=}.
     */
    @GetMapping
    public List<ReservaResponse> listar(@RequestParam(required = false) Long idCliente,
                                        @RequestParam(required = false) Long idSalida,
                                        @RequestParam(required = false) Long idPrestador) {
        if (idCliente != null) {
            return reservaService.listarPorCliente(idCliente);
        }
        if (idSalida != null) {
            return reservaService.listarPorSalida(idSalida);
        }
        if (idPrestador != null) {
            return reservaService.listarPorPrestador(idPrestador);
        }
        return reservaService.listar();
    }

    /** {@code GET /api/reservas/{id}}: obtiene una reserva con participantes y saldo. */
    @GetMapping("/{id}")
    public ReservaResponse obtener(@PathVariable Long id) {
        return reservaService.obtener(id);
    }

    /** {@code POST /api/reservas}: crea una reserva en estado PENDIENTE. Responde 201. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponse crear(@Valid @RequestBody ReservaRequest req) {
        return reservaService.crear(req);
    }

    /**
     * {@code POST /api/reservas/{id}/participantes}: agrega un participante; vuelve a validar
     * cupo, edad y documento, y recalcula el valor total.
     */
    @PostMapping("/{id}/participantes")
    public ReservaResponse agregarParticipante(@PathVariable Long id,
                                               @Valid @RequestBody ParticipanteRequest req) {
        return reservaService.agregarParticipante(id, req);
    }

    /**
     * {@code DELETE /api/reservas/{id}/participantes/{numParticipante}}: quita un participante
     * y recalcula el valor total.
     */
    @DeleteMapping("/{id}/participantes/{numParticipante}")
    public ReservaResponse quitarParticipante(@PathVariable Long id, @PathVariable Integer numParticipante) {
        return reservaService.quitarParticipante(id, numParticipante);
    }

    /** {@code PATCH /api/reservas/{id}/confirmar}: confirma una reserva pendiente. */
    @PatchMapping("/{id}/confirmar")
    public ReservaResponse confirmar(@PathVariable Long id) {
        return reservaService.confirmar(id);
    }

    /**
     * {@code PATCH /api/reservas/{id}/cancelar}: cancela la reserva y devuelve el reembolso.
     * <ul>
     *   <li>Cliente ({@code {"porOperador": false}} o sin cuerpo): 80 % con más de 5 días
     *       de anticipación; si no, 0.</li>
     *   <li>Operador ({@code {"porOperador": true}}): 100 %. Exige el header
     *       {@value Sesion#HEADER_USUARIO} de un usuario interno.</li>
     * </ul>
     */
    @PatchMapping("/{id}/cancelar")
    public CancelacionResponse cancelar(@PathVariable Long id,
                                        @RequestHeader(value = Sesion.HEADER_USUARIO, required = false) Long idUsuario,
                                        @RequestBody(required = false) CancelarReservaRequest req) {
        boolean porOperador = req != null && req.porOperador();
        if (porOperador) {
            autorizacion.exigirUsuarioInterno(idUsuario);
        }
        return reservaService.cancelar(id, porOperador);
    }
}
