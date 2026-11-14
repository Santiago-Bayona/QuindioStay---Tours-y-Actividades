package co.edu.uniquindio.QuindioStay_TyA.dto.response;

import co.edu.uniquindio.QuindioStay_TyA.domain.UsuarioSistema;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoUsuario;

/**
 * Usuario interno devuelto por la API. Nunca incluye la contraseña.
 */
public record UsuarioResponse(
        Long idUsuario,
        String nombreUsuario,
        EstadoUsuario estado,
        Long idRol,
        String rol,
        Long idPrestador,
        String prestador
) {

    public static UsuarioResponse de(UsuarioSistema u) {
        return new UsuarioResponse(
                u.getIdUsuario(), u.getNombreUsuario(), u.getEstado(),
                u.getRol().getIdRol(), u.getRol().getNombre(),
                u.getPrestador() == null ? null : u.getPrestador().getIdPrestador(),
                u.getPrestador() == null ? null : u.getPrestador().getNombreComercial());
    }
}
