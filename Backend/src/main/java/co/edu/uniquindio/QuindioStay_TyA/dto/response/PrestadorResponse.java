package co.edu.uniquindio.QuindioStay_TyA.dto.response;

import co.edu.uniquindio.QuindioStay_TyA.domain.Prestador;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPrestador;

/**
 * Prestador devuelto por la API, con el nombre de su municipio y de su tipo.
 */
public record PrestadorResponse(
        Long idPrestador,
        String nombreComercial,
        String nitDocumento,
        String numeroRnt,
        String direccion,
        String telefono,
        String correo,
        EstadoPrestador estado,
        Long idMunicipio,
        String municipio,
        Long idTipoPrestador,
        String tipoPrestador
) {

    public static PrestadorResponse de(Prestador p) {
        return new PrestadorResponse(
                p.getIdPrestador(), p.getNombreComercial(), p.getNitDocumento(), p.getNumeroRnt(),
                p.getDireccion(), p.getTelefono(), p.getCorreo(), p.getEstado(),
                p.getMunicipio().getIdMunicipio(), p.getMunicipio().getNombre(),
                p.getTipoPrestador().getIdTipoPrestador(), p.getTipoPrestador().getNombreTipo());
    }
}
