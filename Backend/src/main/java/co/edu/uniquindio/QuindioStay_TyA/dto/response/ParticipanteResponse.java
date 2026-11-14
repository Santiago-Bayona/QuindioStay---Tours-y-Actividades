package co.edu.uniquindio.QuindioStay_TyA.dto.response;

import co.edu.uniquindio.QuindioStay_TyA.domain.Participante;

import java.time.LocalDate;

/**
 * Participante devuelto por la API.
 */
public record ParticipanteResponse(Integer numParticipante, String nombre, String documento, LocalDate fechaNacimiento) {

    public static ParticipanteResponse de(Participante p) {
        return new ParticipanteResponse(p.getNumParticipante(), p.getNombre(), p.getDocumento(), p.getFechaNacimiento());
    }
}
