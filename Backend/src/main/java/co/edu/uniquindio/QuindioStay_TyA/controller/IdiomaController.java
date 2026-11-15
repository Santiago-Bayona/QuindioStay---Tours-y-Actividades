package co.edu.uniquindio.QuindioStay_TyA.controller;

import co.edu.uniquindio.QuindioStay_TyA.dto.request.CatalogoRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.CatalogoResponse;
import co.edu.uniquindio.QuindioStay_TyA.service.AutorizacionService;
import co.edu.uniquindio.QuindioStay_TyA.service.IdiomaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD del catálogo de idiomas.
 * <p>
 * Consultar es libre (lo usan los formularios del frontend). Crear, actualizar y
 * eliminar solo los puede hacer el administrador, identificado con el header
 * {@value Sesion#HEADER_USUARIO}.
 */
@RestController
@RequestMapping("/api/idiomas")
@RequiredArgsConstructor
public class IdiomaController {

    private final IdiomaService servicio;
    private final AutorizacionService autorizacion;

    /** {@code GET /api/idiomas}: lista todos los registros. */
    @GetMapping
    public List<CatalogoResponse> listar() {
        return servicio.listar();
    }

    /** {@code GET /api/idiomas/{id}}: obtiene uno por su id (404 si no existe). */
    @GetMapping("/{id}")
    public CatalogoResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    /** {@code POST /api/idiomas}: crea un registro (solo administrador). Responde 201. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogoResponse crear(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                         @Valid @RequestBody CatalogoRequest req) {
        autorizacion.exigirAdministrador(idUsuario);
        return servicio.crear(req);
    }

    /** {@code PUT /api/idiomas/{id}}: actualiza un registro (solo administrador). */
    @PutMapping("/{id}")
    public CatalogoResponse actualizar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                         @PathVariable Long id,
                         @Valid @RequestBody CatalogoRequest req) {
        autorizacion.exigirAdministrador(idUsuario);
        return servicio.actualizar(id, req);
    }

    /**
     * {@code DELETE /api/idiomas/{id}}: elimina un registro (solo administrador). Responde 204.
     * Si otros datos dependen de él, la base lo impide y se responde 409.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario, @PathVariable Long id) {
        autorizacion.exigirAdministrador(idUsuario);
        servicio.eliminar(id);
    }
}
