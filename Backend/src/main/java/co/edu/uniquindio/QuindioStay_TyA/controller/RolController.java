package co.edu.uniquindio.QuindioStay_TyA.controller;

import co.edu.uniquindio.QuindioStay_TyA.dto.response.CatalogoResponse;
import co.edu.uniquindio.QuindioStay_TyA.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consulta de roles. Son fijos (administrador, encargado de prestador, operador),
 * por eso solo hay lectura: el frontend los usa para el formulario de usuarios.
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    /** {@code GET /api/roles}: lista los roles. */
    @GetMapping
    public List<CatalogoResponse> listar() {
        return rolService.listar();
    }

    /** {@code GET /api/roles/{id}}: obtiene un rol. */
    @GetMapping("/{id}")
    public CatalogoResponse obtener(@PathVariable Long id) {
        return rolService.obtener(id);
    }
}
