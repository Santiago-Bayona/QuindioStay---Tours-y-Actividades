package co.edu.uniquindio.QuindioStay_TyA.dto.response;

import co.edu.uniquindio.QuindioStay_TyA.domain.Cliente;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.TipoDocumento;

import java.time.LocalDate;

/**
 * Cliente devuelto por la API.
 */
public record ClienteResponse(
        Long idCliente,
        String nombre,
        String apellido,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String correo,
        String telefono,
        LocalDate fechaNacimiento,
        String ciudadOrigen,
        PerfilClienteResponse perfil
) {

    public static ClienteResponse de(Cliente c) {
        return new ClienteResponse(
                c.getIdCliente(), c.getNombre(), c.getApellido(), c.getTipoDocumento(), c.getNumeroDocumento(),
                c.getCorreo(), c.getTelefono(), c.getFechaNacimiento(), c.getCiudadOrigen(),
                PerfilClienteResponse.de(c.getPerfil()));
    }
}
