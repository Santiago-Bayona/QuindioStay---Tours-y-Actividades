package co.edu.uniquindio.QuindioStay_TyA.controller;

import co.edu.uniquindio.QuindioStay_TyA.dto.request.TourRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.TourResponse;
import co.edu.uniquindio.QuindioStay_TyA.service.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Publicación y consulta de tours.
 * <p>
 * El catálogo ({@code /disponibles}) solo muestra tours de prestadores activos.
 * Publicar o editar un tour lo puede hacer el administrador o el encargado del
 * prestador; un tour con salidas no se elimina.
 */
@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    /**
     * {@code GET /api/tours}: lista los tours. Filtros opcionales:
     * {@code ?idCategoria=3} o {@code ?idPrestador=1} (si llegan ambos, gana la categoría).
     */
    @GetMapping
    public List<TourResponse> listar(@RequestParam(required = false) Long idCategoria,
                                     @RequestParam(required = false) Long idPrestador) {
        if (idCategoria != null) {
            return tourService.listarPorCategoria(idCategoria);
        }
        if (idPrestador != null) {
            return tourService.listarPorPrestador(idPrestador);
        }
        return tourService.listar();
    }

    /** {@code GET /api/tours/disponibles}: catálogo visible para el viajero. */
    @GetMapping("/disponibles")
    public List<TourResponse> listarDisponibles() {
        return tourService.listarDisponibles();
    }

    /** {@code GET /api/tours/{id}}: obtiene un tour. */
    @GetMapping("/{id}")
    public TourResponse obtener(@PathVariable Long id) {
        return tourService.obtener(id);
    }

    /** {@code POST /api/tours}: publica un tour. Responde 201. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TourResponse crear(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                              @Valid @RequestBody TourRequest req) {
        return tourService.crear(idUsuario, req);
    }

    /** {@code PUT /api/tours/{id}}: actualiza un tour. */
    @PutMapping("/{id}")
    public TourResponse actualizar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                   @PathVariable Long id,
                                   @Valid @RequestBody TourRequest req) {
        return tourService.actualizar(idUsuario, id, req);
    }

    /** {@code DELETE /api/tours/{id}}: elimina un tour sin salidas. Responde 204. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario, @PathVariable Long id) {
        tourService.eliminar(idUsuario, id);
    }
}
