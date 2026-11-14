package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.Cliente;
import co.edu.uniquindio.QuindioStay_TyA.domain.UsuarioSistema;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.LoginRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.LoginResponse;
import co.edu.uniquindio.QuindioStay_TyA.exception.AutenticacionException;
import co.edu.uniquindio.QuindioStay_TyA.repository.ClienteRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.UsuarioSistemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicio de sesión de usuarios internos y de clientes.
 * Por seguridad, cuando el usuario no existe o la contraseña es incorrecta se
 * devuelve el mismo mensaje ("Usuario o contraseña incorrectos") para no revelar
 * qué cuentas existen. El token o la sesión HTTP los define la capa web.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UsuarioSistemaRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse loginUsuario(LoginRequest req) {
        UsuarioSistema usuario = usuarioRepository.buscarPorNombreUsuario(req.usuario().trim())
                .orElseThrow(AutenticacionException::new);
        if (!passwordEncoder.matches(req.contrasena(), usuario.getContrasenaCifrada())) {
            throw new AutenticacionException();
        }
        if (!usuario.estaActivo()) {
            throw new AutenticacionException("El usuario está bloqueado");
        }
        Long idPrestador = usuario.getPrestador() == null ? null : usuario.getPrestador().getIdPrestador();
        return new LoginResponse(LoginResponse.TIPO_USUARIO, usuario.getIdUsuario(),
                usuario.getNombreUsuario(), usuario.getRol().getNombre(), idPrestador);
    }

    public LoginResponse loginCliente(LoginRequest req) {
        Cliente cliente = clienteRepository.buscarPorCorreoCuenta(req.usuario().trim())
                .orElseThrow(AutenticacionException::new);
        if (!passwordEncoder.matches(req.contrasena(), cliente.getPerfil().getContrasenaCifrada())) {
            throw new AutenticacionException();
        }
        return new LoginResponse(LoginResponse.TIPO_CLIENTE, cliente.getIdCliente(),
                cliente.nombreCompleto(), null, null);
    }
}
