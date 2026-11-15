package co.edu.uniquindio.QuindioStay_TyA.controller;

import co.edu.uniquindio.QuindioStay_TyA.dto.request.GuiaRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.GuiaResponse;
import co.edu.uniquindio.QuindioStay_TyA.service.AutorizacionService;
import co.edu.uniquindio.QuindioStay_TyA.service.GuiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestión de guías y de los idiomas que hablan.
 * <p>
 * Los idiomas se envían como lista de ids en {@code idIdiomas} al crear o editar;
 * el service actualiza la tabla {@code guia_idioma}. Modificar guías es solo para
 * usuarios internos. Un guía con salidas asignadas no se elimina.
 */
@RestController
@RequestMapping("/api/guias")
@RequiredArgsConstructor
public class GuiaController {

    private final GuiaService guiaService;
    private final AutorizacionService autorizacion;

    /**
     * {@code GET /api/guias}: lista los guías. Con {@code ?idIdioma=2} devuelve solo los
     * que hablan ese idioma (útil al programar una salida en ese idioma).
     */
    @GetMapping
    public List<GuiaResponse> listar(@RequestParam(required = false) Long idIdioma) {
        if (idIdioma != null) {
            return guiaService.listarPorIdioma(idIdioma);
        }
        return guiaService.listar();
    }

    /** {@code GET /api/guias/{id}}: obtiene un guía con sus idiomas. */
    @GetMapping("/{id}")
    public GuiaResponse obtener(@PathVariable Long id) {
        return guiaService.obtener(id);
    }

    /** {@code POST /api/guias}: registra un guía. Responde 201. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuiaResponse crear(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                              @Valid @RequestBody GuiaRequest req) {
        autorizacion.exigirUsuarioInterno(idUsuario);
        return guiaService.crear(req);
    }

    /** {@code PUT /api/guias/{id}}: actualiza un guía y su lista de idiomas. */
    @PutMapping("/{id}")
    public GuiaResponse actualizar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                   @PathVariable Long id,
                                   @Valid @RequestBody GuiaRequest req) {
        autorizacion.exigirUsuarioInterno(idUsuario);
        return guiaService.actualizar(id, req);
    }

    /** {@code DELETE /api/guias/{id}}: elimina un guía sin salidas. Responde 204. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario, @PathVariable Long id) {
        autorizacion.exigirUsuarioInterno(idUsuario);
        guiaService.eliminar(id);
    }
}
