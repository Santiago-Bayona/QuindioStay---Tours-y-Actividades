package co.edu.uniquindio.QuindioStay_TyA.controller;

import co.edu.uniquindio.QuindioStay_TyA.dto.request.SalidaTourRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.SalidaTourResponse;
import co.edu.uniquindio.QuindioStay_TyA.service.SalidaTourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Programación de salidas de los tours.
 * <p>
 * Al crear o editar, el service valida que el guía hable el idioma de la salida y
 * que no tenga otra salida que se cruce. Cancelar una salida cancela sus reservas
 * con reembolso del 100 %. Cada respuesta incluye los cupos ocupados y disponibles.
 */
@RestController
@RequestMapping("/api/salidas")
@RequiredArgsConstructor
public class SalidaTourController {

    private final SalidaTourService salidaService;

    /**
     * {@code GET /api/salidas}: lista las salidas. Filtros opcionales:
     * {@code ?idTour=1} o {@code ?idGuia=2} (si llegan ambos, gana el tour).
     */
    @GetMapping
    public List<SalidaTourResponse> listar(@RequestParam(required = false) Long idTour,
                                           @RequestParam(required = false) Long idGuia) {
        if (idTour != null) {
            return salidaService.listarPorTour(idTour);
        }
        if (idGuia != null) {
            return salidaService.listarPorGuia(idGuia);
        }
        return salidaService.listar();
    }

    /** {@code GET /api/salidas/disponibles}: salidas programadas y futuras que aceptan reservas. */
    @GetMapping("/disponibles")
    public List<SalidaTourResponse> listarDisponibles() {
        return salidaService.listarDisponibles();
    }

    /** {@code GET /api/salidas/{id}}: obtiene una salida con su disponibilidad. */
    @GetMapping("/{id}")
    public SalidaTourResponse obtener(@PathVariable Long id) {
        return salidaService.obtener(id);
    }

    /** {@code POST /api/salidas}: programa una salida. Responde 201. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalidaTourResponse crear(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                    @Valid @RequestBody SalidaTourRequest req) {
        return salidaService.crear(idUsuario, req);
    }

    /** {@code PUT /api/salidas/{id}}: modifica fecha, hora, cupo, guía o idioma de una salida. */
    @PutMapping("/{id}")
    public SalidaTourResponse actualizar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                         @PathVariable Long id,
                                         @Valid @RequestBody SalidaTourRequest req) {
        return salidaService.actualizar(idUsuario, id, req);
    }

    /**
     * {@code PATCH /api/salidas/{id}/cancelar}: el operador cancela la salida; sus reservas
     * activas se cancelan con reembolso del 100 %.
     */
    @PatchMapping("/{id}/cancelar")
    public SalidaTourResponse cancelar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                       @PathVariable Long id) {
        return salidaService.cancelar(idUsuario, id);
    }

    /** {@code PATCH /api/salidas/{id}/realizada}: marca la salida como realizada. */
    @PatchMapping("/{id}/realizada")
    public SalidaTourResponse marcarRealizada(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                              @PathVariable Long id) {
        return salidaService.marcarRealizada(idUsuario, id);
    }

    /** {@code DELETE /api/salidas/{id}}: elimina una salida sin reservas. Responde 204. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario, @PathVariable Long id) {
        salidaService.eliminar(idUsuario, id);
    }
}
