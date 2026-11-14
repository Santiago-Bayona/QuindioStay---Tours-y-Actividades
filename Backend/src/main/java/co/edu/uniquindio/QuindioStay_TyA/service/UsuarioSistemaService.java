package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.Prestador;
import co.edu.uniquindio.QuindioStay_TyA.domain.Rol;
import co.edu.uniquindio.QuindioStay_TyA.domain.UsuarioSistema;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoUsuario;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.CambiarContrasenaRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.request.UsuarioRequest;
import co.edu.uniquindio.QuindioStay_TyA.dto.response.UsuarioResponse;
import co.edu.uniquindio.QuindioStay_TyA.exception.AutenticacionException;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.exception.ReglaNegocioException;
import co.edu.uniquindio.QuindioStay_TyA.repository.PrestadorRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.RolRepository;
import co.edu.uniquindio.QuindioStay_TyA.repository.UsuarioSistemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestión de usuarios internos (solo el administrador).
 *   El nombre de usuario es único y la contraseña se guarda cifrada con BCrypt.
 *   El rol "Encargado de prestador" exige prestador; los demás roles no lo llevan.
 *   Un administrador no puede bloquear ni borrar su propia cuenta.
 *
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioSistemaService {

    private final UsuarioSistemaRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PrestadorRepository prestadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AutorizacionService autorizacion;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(Long idUsuarioActor) {
        autorizacion.exigirAdministrador(idUsuarioActor);
        return usuarioRepository.findAll(Sort.by("nombreUsuario")).stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarPorPrestador(Long idUsuarioActor, Long idPrestador) {
        autorizacion.exigirGestionPrestador(idUsuarioActor, idPrestador);
        return usuarioRepository.listarPorPrestador(idPrestador).stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long idUsuarioActor, Long id) {
        autorizacion.exigirAdministrador(idUsuarioActor);
        return UsuarioResponse.de(buscar(id));
    }

    public UsuarioResponse crear(Long idUsuarioActor, UsuarioRequest req) {
        autorizacion.exigirAdministrador(idUsuarioActor);
        if (req.contrasena() == null || req.contrasena().isBlank()) {
            throw new ReglaNegocioException("La contraseña es obligatoria al crear el usuario");
        }
        validarNombreUnico(req.nombreUsuario().trim(), null);
        UsuarioSistema usuario = new UsuarioSistema();
        usuario.setEstado(EstadoUsuario.ACTIVO);
        copiarDatos(usuario, req);
        usuario.setContrasenaCifrada(passwordEncoder.encode(req.contrasena()));
        return UsuarioResponse.de(usuarioRepository.saveAndFlush(usuario));
    }

    public UsuarioResponse actualizar(Long idUsuarioActor, Long id, UsuarioRequest req) {
        autorizacion.exigirAdministrador(idUsuarioActor);
        UsuarioSistema usuario = buscar(id);
        validarNombreUnico(req.nombreUsuario().trim(), id);
        copiarDatos(usuario, req);
        if (req.contrasena() != null && !req.contrasena().isBlank()) {
            usuario.setContrasenaCifrada(passwordEncoder.encode(req.contrasena()));
        }
        return UsuarioResponse.de(usuarioRepository.saveAndFlush(usuario));
    }

    public UsuarioResponse cambiarEstado(Long idUsuarioActor, Long id, EstadoUsuario nuevoEstado) {
        autorizacion.exigirAdministrador(idUsuarioActor);
        UsuarioSistema usuario = buscar(id);
        if (nuevoEstado == EstadoUsuario.BLOQUEADO) {
            if (idUsuarioActor.equals(id)) {
                throw new ReglaNegocioException("No puede bloquear su propia cuenta");
            }
            usuario.bloquear();
        } else {
            usuario.desbloquear();
        }
        return UsuarioResponse.de(usuarioRepository.saveAndFlush(usuario));
    }

    /** Cada usuario cambia su propia contraseña, confirmando la actual. */
    public void cambiarContrasena(Long idUsuario, CambiarContrasenaRequest req) {
        autorizacion.exigirUsuarioInterno(idUsuario);
        UsuarioSistema usuario = buscar(idUsuario);
        if (!passwordEncoder.matches(req.actual(), usuario.getContrasenaCifrada())) {
            throw new AutenticacionException("La contraseña actual no es correcta");
        }
        usuario.setContrasenaCifrada(passwordEncoder.encode(req.nueva()));
        usuarioRepository.saveAndFlush(usuario);
    }

    public void eliminar(Long idUsuarioActor, Long id) {
        autorizacion.exigirAdministrador(idUsuarioActor);
        if (idUsuarioActor.equals(id)) {
            throw new ReglaNegocioException("No puede borrar su propia cuenta");
        }
        usuarioRepository.delete(buscar(id));
        usuarioRepository.flush();
    }

    private UsuarioSistema buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el usuario", id));
    }

    private void validarNombreUnico(String nombre, Long idActual) {
        usuarioRepository.buscarPorNombreUsuario(nombre)
                .filter(u -> !u.getIdUsuario().equals(idActual))
                .ifPresent(u -> {
                    throw new ReglaNegocioException("Ese nombre de usuario ya está en uso");
                });
    }

    private void copiarDatos(UsuarioSistema usuario, UsuarioRequest req) {
        Rol rol = rolRepository.findById(req.idRol())
                .orElseThrow(() -> new RecursoNoEncontradoException("el rol", req.idRol()));
        Prestador prestador = null;
        if (req.idPrestador() != null) {
            prestador = prestadorRepository.findById(req.idPrestador())
                    .orElseThrow(() -> new RecursoNoEncontradoException("el prestador", req.idPrestador()));
        }
        boolean esEncargado = rol.esRol(Rol.ENCARGADO_PRESTADOR);
        if (esEncargado && prestador == null) {
            throw new ReglaNegocioException("El encargado de prestador debe tener un prestador asignado");
        }
        if (!esEncargado && prestador != null) {
            throw new ReglaNegocioException("Solo el rol 'Encargado de prestador' se asocia a un prestador");
        }
        usuario.setNombreUsuario(req.nombreUsuario().trim());
        usuario.setRol(rol);
        usuario.setPrestador(prestador);
    }
}
