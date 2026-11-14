package co.edu.uniquindio.QuindioStay_TyA.dto.response;

import co.edu.uniquindio.QuindioStay_TyA.domain.SalidaTour;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoSalida;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Salida devuelta por la API, con el cupo ocupado y el disponible.
 */
public record SalidaTourResponse(
        Long idSalida,
        LocalDate fechaSalida,
        LocalTime horaSalida,
        Integer cupo,
        long cuposOcupados,
        long cuposDisponibles,
        EstadoSalida estado,
        Long idTour,
        String tour,
        Long idGuia,
        String guia,
        Long idIdioma,
        String idioma
) {

    public static SalidaTourResponse de(SalidaTour s, long cuposOcupados) {
        return new SalidaTourResponse(
                s.getIdSalida(), s.getFechaSalida(), s.getHoraSalida(), s.getCupo(),
                cuposOcupados, Math.max(0, s.getCupo() - cuposOcupados), s.getEstado(),
                s.getTour().getIdTour(), s.getTour().getNombre(),
                s.getGuia().getIdGuia(), s.getGuia().getNombre(),
                s.getIdioma().getIdIdioma(), s.getIdioma().getNombreIdioma());
    }
}
