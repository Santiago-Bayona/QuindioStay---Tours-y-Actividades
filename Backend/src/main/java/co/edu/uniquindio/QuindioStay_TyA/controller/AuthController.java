package co.edu.uniquindio.QuindioStay_TyA.controller;

import co.edu.uniquindio.QuindioStay_TyA.dto.request.LoginRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.LoginResponse;
import co.edu.uniquindio.QuindioStay_TyA.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inicio de sesión de usuarios internos y de clientes.
 * <p>
 * Ambos devuelven un {@link LoginResponse} con el {@code id}, el nombre y, para
 * usuarios internos, el rol y el prestador. El frontend guarda esa respuesta y
 * usa el {@code id} como sesión. Si las credenciales fallan responde 401.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Inicia sesión de un usuario interno (administrador, encargado u operador).
     * <p>
     * {@code POST /api/auth/login/usuario} con {@code {"usuario": "admin", "contrasena": "..."}}
     */
    @PostMapping("/login/usuario")
    public LoginResponse loginUsuario(@Valid @RequestBody LoginRequest req) {
        return authService.loginUsuario(req);
    }

    /**
     * Inicia sesión de un cliente con el correo de su cuenta.
     * <p>
     * {@code POST /api/auth/login/cliente} con {@code {"usuario": "correo@cuenta.com", "contrasena": "..."}}
     */
    @PostMapping("/login/cliente")
    public LoginResponse loginCliente(@Valid @RequestBody LoginRequest req) {
        return authService.loginCliente(req);
    }
}
