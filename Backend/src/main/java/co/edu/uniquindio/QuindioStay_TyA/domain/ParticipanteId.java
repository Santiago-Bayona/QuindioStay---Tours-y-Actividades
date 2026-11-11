package co.edu.uniquindio.QuindioStay_TyA.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Clave primaria compuesta de {@link Participante}: (id_reserva, num_participante).
 * <p>
 * JPA exige que sea {@code Serializable} y que implemente {@code equals} y
 * {@code hashCode}, generados aquí con Lombok.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class ParticipanteId implements Serializable {

    /** Reserva a la que pertenece; lo llena JPA gracias a {@code @MapsId}. */
    @Column(name = "id_reserva")
    private Long idReserva;

    /** Número consecutivo del participante dentro de la reserva. */
    @Column(name = "num_participante")
    private Integer numParticipante;

    /**
     * @param idReserva       id de la reserva (puede ser {@code null} si aún no existe)
     * @param numParticipante número consecutivo dentro de la reserva
     */
    public ParticipanteId(Long idReserva, Integer numParticipante) {
        this.idReserva = idReserva;
        this.numParticipante = numParticipante;
    }
}
