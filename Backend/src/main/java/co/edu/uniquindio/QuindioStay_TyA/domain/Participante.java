package co.edu.uniquindio.QuindioStay_TyA.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Period;

/**
 * Persona que asiste a una salida dentro de una reserva.
 * <p>
 * Tabla: {@code participante}. Entidad débil: su clave es (id_reserva,
 * num_participante) y no existe sin su reserva. Se crea siempre con
 * {@link Reserva#agregarParticipante(Participante)}.
 */
@Entity
@Table(name = "participante")
@Getter
@Setter
@NoArgsConstructor
public class Participante {

    /** Mayoría de edad en Colombia. */
    public static final int MAYORIA_DE_EDAD = 18;

    /** Clave compuesta. */
    @EmbeddedId
    private ParticipanteId id = new ParticipanteId();

    /** Reserva a la que pertenece; alimenta {@code id.idReserva}. */
    @MapsId("idReserva")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    /** Nombre completo. */
    @Column(nullable = false, length = 120)
    private String nombre;

    /** Documento de identidad; no se repite dentro de la misma reserva. */
    @Column(nullable = false, length = 30)
    private String documento;

    /** Fecha de nacimiento; permite calcular la edad en la fecha de la salida. */
    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    /**
     * Crea un participante sin reserva; se asocia con {@link Reserva#agregarParticipante}.
     *
     * @param nombre          nombre completo
     * @param documento       documento de identidad
     * @param fechaNacimiento fecha de nacimiento
     */
    public Participante(String nombre, String documento, LocalDate fechaNacimiento) {
        this.nombre = nombre;
        this.documento = documento;
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * Asocia el participante a una reserva con su número consecutivo.
     * Solo lo usa {@link Reserva#agregarParticipante}.
     *
     * @param reserva         reserva dueña
     * @param numParticipante número consecutivo
     */
    void asignarAReserva(Reserva reserva, int numParticipante) {
        this.reserva = reserva;
        this.id = new ParticipanteId(reserva.getIdReserva(), numParticipante);
    }

    /**
     * @return número consecutivo del participante dentro de la reserva
     */
    public Integer getNumParticipante() {
        return id.getNumParticipante();
    }

    /**
     * Calcula la edad del participante en una fecha dada.
     *
     * @param fecha fecha de referencia; normalmente la fecha de la salida
     * @return años cumplidos en esa fecha
     */
    public int calcularEdad(LocalDate fecha) {
        return Period.between(fechaNacimiento, fecha).getYears();
    }

    /**
     * Indica si el participante cumple la edad mínima del tour en la fecha de la salida.
     *
     * @return {@code true} si puede asistir
     */
    public boolean cumpleEdadMinima() {
        SalidaTour salida = reserva.getSalida();
        return salida.getTour().permiteEdad(calcularEdad(salida.getFechaSalida()));
    }

    /**
     * Indica si el participante es menor de edad en una fecha. Se usa para
     * generar la autorización de actividad para menores.
     *
     * @param fecha fecha de referencia
     * @return {@code true} si tiene menos de 18 años
     */
    public boolean esMenorDeEdad(LocalDate fecha) {
        return calcularEdad(fecha) < MAYORIA_DE_EDAD;
    }
}
