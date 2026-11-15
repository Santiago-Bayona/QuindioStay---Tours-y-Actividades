package co.edu.uniquindio.QuindioStay_TyA.controller;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPrestador;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.PrestadorRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.PrestadorResponse;
import co.edu.uniquindio.QuindioStay_TyA.service.PrestadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestión de prestadores turísticos.
 * <p>
 * Un prestador nunca se elimina: se cambia su estado a SUSPENDIDO o RETIRADO
 * (al retirarlo se inactivan sus planes). Las escrituras validan en el service
 * que el usuario del header {@value Sesion#HEADER_USUARIO} tenga permiso.
 */
@RestController
@RequestMapping("/api/prestadores")
@RequiredArgsConstructor
public class PrestadorController {

    private final PrestadorService prestadorService;

    /**
     * {@code GET /api/prestadores}: lista los prestadores. Filtros opcionales:
     * {@code ?idMunicipio=1} o {@code ?idTipoPrestador=2} (si llegan ambos, gana el municipio).
     */
    @GetMapping
    public List<PrestadorResponse> listar(@RequestParam(required = false) Long idMunicipio,
                                          @RequestParam(required = false) Long idTipoPrestador) {
        if (idMunicipio != null) {
            return prestadorService.listarPorMunicipio(idMunicipio);
        }
        if (idTipoPrestador != null) {
            return prestadorService.listarPorTipo(idTipoPrestador);
        }
        return prestadorService.listar();
    }

    /** {@code GET /api/prestadores/{id}}: obtiene un prestador. */
    @GetMapping("/{id}")
    public PrestadorResponse obtener(@PathVariable Long id) {
        return prestadorService.obtener(id);
    }

    /** {@code POST /api/prestadores}: registra un prestador. Responde 201. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrestadorResponse crear(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                   @Valid @RequestBody PrestadorRequest req) {
        return prestadorService.crear(idUsuario, req);
    }

    /** {@code PUT /api/prestadores/{id}}: actualiza los datos de un prestador. */
    @PutMapping("/{id}")
    public PrestadorResponse actualizar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                        @PathVariable Long id,
                                        @Valid @RequestBody PrestadorRequest req) {
        return prestadorService.actualizar(idUsuario, id, req);
    }

    /**
     * {@code PATCH /api/prestadores/{id}/estado?estado=RETIRADO}: activa, suspende o retira
     * un prestador. Reemplaza al borrado.
     */
    @PatchMapping("/{id}/estado")
    public PrestadorResponse cambiarEstado(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                           @PathVariable Long id,
                                           @RequestParam EstadoPrestador estado) {
        return prestadorService.cambiarEstado(idUsuario, id, estado);
    }
}
