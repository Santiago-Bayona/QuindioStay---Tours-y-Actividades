package co.edu.uniquindio.QuindioStay_TyA.dto.response;

import co.edu.uniquindio.QuindioStay_TyA.domain.Guia;

import java.util.Comparator;
import java.util.List;

/**
 * Guía devuelto por la API, con los idiomas que habla.
 */
public record GuiaResponse(
        Long idGuia,
        String nombre,
        String documento,
        String tarjetaProfesional,
        List<CatalogoResponse> idiomas
) {

    public static GuiaResponse de(Guia g) {
        List<CatalogoResponse> idiomas = g.getIdiomas().stream()
                .map(i -> new CatalogoResponse(i.getIdIdioma(), i.getNombreIdioma()))
                .sorted(Comparator.comparing(CatalogoResponse::id))
                .toList();
        return new GuiaResponse(g.getIdGuia(), g.getNombre(), g.getDocumento(), g.getTarjetaProfesional(), idiomas);
    }
}
