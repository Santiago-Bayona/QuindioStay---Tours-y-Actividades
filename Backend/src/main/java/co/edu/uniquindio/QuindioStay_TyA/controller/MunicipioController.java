package co.edu.uniquindio.QuindioStay_TyA.controller;

import co.edu.uniquindio.QuindioStay_TyA.dto.request.MunicipioRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.MunicipioResponse;
import co.edu.uniquindio.QuindioStay_TyA.service.AutorizacionService;
import co.edu.uniquindio.QuindioStay_TyA.service.MunicipioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD del catálogo de municipios del Quindío.
 * <p>
 * Consultar es libre (lo usan los formularios del frontend). Crear, actualizar y
 * eliminar solo los puede hacer el administrador, identificado con el header
 * {@value Sesion#HEADER_USUARIO}.
 */
@RestController
@RequestMapping("/api/municipios")
@RequiredArgsConstructor
public class MunicipioController {

    private final MunicipioService servicio;
    private final AutorizacionService autorizacion;

    /** {@code GET /api/municipios}: lista todos los registros. */
    @GetMapping
    public List<MunicipioResponse> listar() {
        return servicio.listar();
    }

    /** {@code GET /api/municipios/{id}}: obtiene uno por su id (404 si no existe). */
    @GetMapping("/{id}")
    public MunicipioResponse obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    /** {@code POST /api/municipios}: crea un registro (solo administrador). Responde 201. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MunicipioResponse crear(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                         @Valid @RequestBody MunicipioRequest req) {
        autorizacion.exigirAdministrador(idUsuario);
        return servicio.crear(req);
    }

    /** {@code PUT /api/municipios/{id}}: actualiza un registro (solo administrador). */
    @PutMapping("/{id}")
    public MunicipioResponse actualizar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                         @PathVariable Long id,
                         @Valid @RequestBody MunicipioRequest req) {
        autorizacion.exigirAdministrador(idUsuario);
        return servicio.actualizar(id, req);
    }

    /**
     * {@code DELETE /api/municipios/{id}}: elimina un registro (solo administrador). Responde 204.
     * Si otros datos dependen de él, la base lo impide y se responde 409.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario, @PathVariable Long id) {
        autorizacion.exigirAdministrador(idUsuario);
        servicio.eliminar(id);
    }
}
