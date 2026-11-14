package co.edu.uniquindio.QuindioStay_TyA.dto.response;

import co.edu.uniquindio.QuindioStay_TyA.domain.Municipio;

/**
 * Municipio devuelto por la API.
 */
public record MunicipioResponse(Long idMunicipio, String nombre, String codigoDane) {

    public static MunicipioResponse de(Municipio m) {
        return new MunicipioResponse(m.getIdMunicipio(), m.getNombre(), m.getCodigoDane());
    }
}
