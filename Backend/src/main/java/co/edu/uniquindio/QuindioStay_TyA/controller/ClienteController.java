package co.edu.uniquindio.QuindioStay_TyA.controller;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.TipoDocumento;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.ClienteRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.ClienteResponse;
import co.edu.uniquindio.QuindioStay_TyA.service.AutorizacionService;
import co.edu.uniquindio.QuindioStay_TyA.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Registro de clientes y administración de su perfil.
 * <p>
 * El registro es público: así un cliente crea su cuenta desde el frontend (si envía
 * el bloque {@code perfil}, queda con cuenta y preferencias; si no, es un cliente
 * de mostrador). Listar y buscar clientes es solo para usuarios internos.
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final AutorizacionService autorizacion;

    /** {@code GET /api/clientes}: lista todos los clientes (solo usuarios internos). */
    @GetMapping
    public List<ClienteResponse> listar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario) {
        autorizacion.exigirUsuarioInterno(idUsuario);
        return clienteService.listar();
    }

    /**
     * {@code GET /api/clientes/buscar?tipo=CEDULA_CIUDADANIA&numero=1094123456}:
     * busca un cliente por su documento (solo usuarios internos, p. ej. en el mostrador).
     */
    @GetMapping("/buscar")
    public ClienteResponse buscarPorDocumento(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                              @RequestParam TipoDocumento tipo,
                                              @RequestParam String numero) {
        autorizacion.exigirUsuarioInterno(idUsuario);
        return clienteService.buscarPorDocumento(tipo, numero);
    }

    /** {@code GET /api/clientes/{id}}: obtiene un cliente con su perfil. */
    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable Long id) {
        return clienteService.obtener(id);
    }

    /** {@code POST /api/clientes}: registra un cliente (público). Responde 201. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse registrar(@Valid @RequestBody ClienteRequest req) {
        return clienteService.crear(req);
    }

    /**
     * {@code PUT /api/clientes/{id}}: actualiza los datos y las preferencias del perfil
     * (idioma, canal, anticipación de recordatorios, promociones).
     */
    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest req) {
        return clienteService.actualizar(id, req);
    }

    /**
     * {@code DELETE /api/clientes/{id}}: elimina un cliente sin reservas (solo usuarios internos).
     * Responde 204; si tiene reservas, 400.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario, @PathVariable Long id) {
        autorizacion.exigirUsuarioInterno(idUsuario);
        clienteService.eliminar(id);
    }
}
