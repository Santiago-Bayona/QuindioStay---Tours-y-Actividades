package co.edu.uniquindio.QuindioStay_TyA.dto.response;

import co.edu.uniquindio.QuindioStay_TyA.domain.Tour;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.Dificultad;

import java.math.BigDecimal;

/**
 * Tour devuelto por la API.
 */
public record TourResponse(
        Long idTour,
        String nombre,
        String descripcion,
        BigDecimal duracionHoras,
        Dificultad dificultad,
        Integer edadMinima,
        BigDecimal precioPersona,
        Long idCategoria,
        String categoria,
        Long idPrestador,
        String prestador
) {

    public static TourResponse de(Tour t) {
        return new TourResponse(
                t.getIdTour(), t.getNombre(), t.getDescripcion(), t.getDuracionHoras(), t.getDificultad(),
                t.getEdadMinima(), t.getPrecioPersona(),
                t.getCategoria().getIdCategoria(), t.getCategoria().getNombre(),
                t.getPrestador().getIdPrestador(), t.getPrestador().getNombreComercial());
    }
}
