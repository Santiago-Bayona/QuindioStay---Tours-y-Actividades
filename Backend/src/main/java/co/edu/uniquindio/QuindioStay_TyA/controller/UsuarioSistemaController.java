package co.edu.uniquindio.QuindioStay_TyA.controller;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoUsuario;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.CambiarContrasenaRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.UsuarioRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.UsuarioResponse;
import co.edu.uniquindio.QuindioStay_TyA.service.UsuarioSistemaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Administración de usuarios internos (administrador, encargados y operadores).
 * <p>
 * Todas las operaciones exigen el header {@value Sesion#HEADER_USUARIO}; el service
 * valida los permisos. La respuesta nunca incluye la contraseña.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioSistemaController {

    private final UsuarioSistemaService usuarioService;

    /** {@code GET /api/usuarios}: lista usuarios; con {@code ?idPrestador=} los de ese prestador. */
    @GetMapping
    public List<UsuarioResponse> listar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                        @RequestParam(required = false) Long idPrestador) {
        if (idPrestador != null) {
            return usuarioService.listarPorPrestador(idUsuario, idPrestador);
        }
        return usuarioService.listar(idUsuario);
    }

    /** {@code GET /api/usuarios/{id}}: obtiene un usuario. */
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario, @PathVariable Long id) {
        return usuarioService.obtener(idUsuario, id);
    }

    /** {@code POST /api/usuarios}: crea un usuario interno. Responde 201. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                 @Valid @RequestBody UsuarioRequest req) {
        return usuarioService.crear(idUsuario, req);
    }

    /** {@code PUT /api/usuarios/{id}}: actualiza un usuario. */
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                      @PathVariable Long id,
                                      @Valid @RequestBody UsuarioRequest req) {
        return usuarioService.actualizar(idUsuario, id, req);
    }

    /** {@code PATCH /api/usuarios/{id}/estado?estado=BLOQUEADO}: bloquea o desbloquea un usuario. */
    @PatchMapping("/{id}/estado")
    public UsuarioResponse cambiarEstado(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                         @PathVariable Long id,
                                         @RequestParam EstadoUsuario estado) {
        return usuarioService.cambiarEstado(idUsuario, id, estado);
    }

    /**
     * {@code PATCH /api/usuarios/contrasena}: el usuario de la sesión cambia su propia
     * contraseña enviando la actual y la nueva. Responde 204.
     */
    @PatchMapping("/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                  @Valid @RequestBody CambiarContrasenaRequest req) {
        usuarioService.cambiarContrasena(idUsuario, req);
    }

    /** {@code DELETE /api/usuarios/{id}}: elimina un usuario. Responde 204. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario, @PathVariable Long id) {
        usuarioService.eliminar(idUsuario, id);
    }
}
