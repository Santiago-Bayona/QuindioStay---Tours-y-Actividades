package co.edu.uniquindio.QuindioStay_TyA.controller;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPlan;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.PlanRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.PlanResponse;
import co.edu.uniquindio.QuindioStay_TyA.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Gestión de los planes que ofrece cada prestador.
 * <p>
 * Solo el administrador o el encargado del prestador dueño del plan pueden
 * modificarlo (lo valida el service con el header {@value Sesion#HEADER_USUARIO}).
 * Un plan que ya se vendió no se borra: se inactiva.
 */
@RestController
@RequestMapping("/api/planes")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    /**
     * {@code GET /api/planes}: lista planes. Filtros opcionales:
     * <ul>
     *   <li>{@code ?idPrestador=1}: planes de un prestador</li>
     *   <li>{@code ?fecha=2026-12-05}: planes vigentes en esa fecha</li>
     *   <li>{@code ?idPrestador=1&fecha=2026-12-05}: los que el cliente puede elegir al reservar</li>
     * </ul>
     */
    @GetMapping
    public List<PlanResponse> listar(@RequestParam(required = false) Long idPrestador,
                                     @RequestParam(required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        if (idPrestador != null && fecha != null) {
            return planService.listarVigentesPorPrestador(idPrestador, fecha);
        }
        if (idPrestador != null) {
            return planService.listarPorPrestador(idPrestador);
        }
        if (fecha != null) {
            return planService.listarVigentes(fecha);
        }
        return planService.listar();
    }

    /** {@code GET /api/planes/{id}}: obtiene un plan. */
    @GetMapping("/{id}")
    public PlanResponse obtener(@PathVariable Long id) {
        return planService.obtener(id);
    }

    /** {@code POST /api/planes}: crea un plan para un prestador. Responde 201. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanResponse crear(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                              @Valid @RequestBody PlanRequest req) {
        return planService.crear(idUsuario, req);
    }

    /** {@code PUT /api/planes/{id}}: actualiza un plan. */
    @PutMapping("/{id}")
    public PlanResponse actualizar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                   @PathVariable Long id,
                                   @Valid @RequestBody PlanRequest req) {
        return planService.actualizar(idUsuario, id, req);
    }

    /** {@code PATCH /api/planes/{id}/estado?estado=INACTIVO}: activa o inactiva un plan. */
    @PatchMapping("/{id}/estado")
    public PlanResponse cambiarEstado(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                      @PathVariable Long id,
                                      @RequestParam EstadoPlan estado) {
        return planService.cambiarEstado(idUsuario, id, estado);
    }

    /**
     * {@code DELETE /api/planes/{id}}: elimina un plan que nunca se vendió. Responde 204;
     * si ya tiene reservas, 400 (debe inactivarse).
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario, @PathVariable Long id) {
        planService.eliminar(idUsuario, id);
    }
}
