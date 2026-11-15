package co.edu.uniquindio.QuindioStay_TyA.controller;

import co.edu.uniquindio.QuindioStay_TyA.dto.request.NotificacionRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.NotificacionResponse;
import co.edu.uniquindio.QuindioStay_TyA.service.AutorizacionService;
import co.edu.uniquindio.QuindioStay_TyA.service.NotificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Historial de notificaciones y envío simulado.
 * <p>
 * Los recordatorios y confirmaciones los generan solos los services de reservas y
 * pagos; aquí se consultan, y los usuarios internos pueden crear notificaciones
 * manuales (p. ej. promociones, solo a clientes que las aceptan) y lanzar el envío.
 */
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final AutorizacionService autorizacion;

    /** {@code GET /api/notificaciones}: lista todas; con {@code ?idCliente=} el historial del cliente. */
    @GetMapping
    public List<NotificacionResponse> listar(@RequestParam(required = false) Long idCliente) {
        if (idCliente != null) {
            return notificacionService.listarPorCliente(idCliente);
        }
        return notificacionService.listar();
    }

    /** {@code GET /api/notificaciones/pendientes}: pendientes cuya fecha programada ya llegó. */
    @GetMapping("/pendientes")
    public List<NotificacionResponse> listarPendientes() {
        return notificacionService.listarPendientes();
    }

    /** {@code GET /api/notificaciones/{id}}: obtiene una notificación. */
    @GetMapping("/{id}")
    public NotificacionResponse obtener(@PathVariable Long id) {
        return notificacionService.obtener(id);
    }

    /** {@code POST /api/notificaciones}: crea una notificación manual (solo usuarios internos). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificacionResponse crear(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                      @Valid @RequestBody NotificacionRequest req) {
        autorizacion.exigirUsuarioInterno(idUsuario);
        return notificacionService.crear(req);
    }

    /**
     * {@code PATCH /api/notificaciones/{id}/reprogramar?fecha=2026-12-01T08:00:00}:
     * cambia la fecha programada de una notificación pendiente (solo usuarios internos).
     */
    @PatchMapping("/{id}/reprogramar")
    public NotificacionResponse reprogramar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                            @PathVariable Long id,
                                            @RequestParam
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
        autorizacion.exigirUsuarioInterno(idUsuario);
        return notificacionService.reprogramar(id, fecha);
    }

    /** {@code PATCH /api/notificaciones/{id}/leida}: el cliente marca la notificación como leída. */
    @PatchMapping("/{id}/leida")
    public NotificacionResponse marcarLeida(@PathVariable Long id) {
        return notificacionService.marcarLeida(id);
    }

    /**
     * {@code POST /api/notificaciones/enviar-pendientes}: simula el envío de todas las
     * pendientes cuya fecha ya llegó (solo usuarios internos).
     *
     * @return {@code {"enviadas": n}}
     */
    @PostMapping("/enviar-pendientes")
    public Map<String, Integer> enviarPendientes(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario) {
        autorizacion.exigirUsuarioInterno(idUsuario);
        return Map.of("enviadas", notificacionService.enviarPendientes());
    }

    /** {@code DELETE /api/notificaciones/{id}}: elimina una notificación pendiente. Responde 204. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario, @PathVariable Long id) {
        autorizacion.exigirUsuarioInterno(idUsuario);
        notificacionService.eliminar(id);
    }
}
