package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.domain.UsuarioSistema;
import co.edu.uniquindio.QuindioStay_TyA.exception.AccesoDenegadoException;
import co.edu.uniquindio.QuindioStay_TyA.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.QuindioStay_TyA.repository.UsuarioSistemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas de permisos por rol. Los services que modifican datos reciben el id
 * del usuario que ejecuta la operación (el que viene de la sesión) y le piden
 * a este servicio que valide si puede hacerlo.
 *
 *   Administrador: puede todo.</li>
 *   Encargado de prestador: solo gestiona datos de su prestador.</li>
 *   Operador: opera salidas y reservas, pero no administra prestadores ni usuarios.</li>
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutorizacionService {

    private final UsuarioSistemaRepository usuarioRepository;

    /** Carga al usuario y verifica que exista y no esté bloqueado. */
    public UsuarioSistema usuarioActivo(Long idUsuario) {
        if (idUsuario == null) {
            throw new AccesoDenegadoException("Debe iniciar sesión para realizar esta operación");
        }
        UsuarioSistema usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new AccesoDenegadoException("La sesión no corresponde a un usuario válido"));
        if (!usuario.estaActivo()) {
            throw new AccesoDenegadoException("El usuario está bloqueado");
        }
        return usuario;
    }

    public void exigirAdministrador(Long idUsuario) {
        if (!usuarioActivo(idUsuario).esAdministrador()) {
            throw new AccesoDenegadoException("Solo el administrador de la plataforma puede realizar esta operación");
        }
    }

    /** Administrador, o el encargado del prestador indicado. */
    public void exigirGestionPrestador(Long idUsuario, Long idPrestador) {
        if (!usuarioActivo(idUsuario).puedeGestionarPrestador(idPrestador)) {
            throw new AccesoDenegadoException("No tiene permisos para gestionar los datos de este prestador");
        }
    }

    /** Administrador, operador, o el encargado del prestador dueño del tour. */
    public void exigirOperacionSalida(Long idUsuario, Long idPrestador) {
        UsuarioSistema usuario = usuarioActivo(idUsuario);
        if (usuario.esAdministrador() || usuario.esOperador() || usuario.puedeGestionarPrestador(idPrestador)) {
            return;
        }
        throw new AccesoDenegadoException("No tiene permisos para gestionar las salidas de este prestador");
    }

    /** Verifica que el usuario exista y esté activo, sin importar su rol. */
    public void exigirUsuarioInterno(Long idUsuario) {
        usuarioActivo(idUsuario);
    }
}
