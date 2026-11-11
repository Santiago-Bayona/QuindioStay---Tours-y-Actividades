package co.edu.uniquindio.QuindioStay_TyA.domain;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoSalida;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Ocasión concreta en que se realiza un tour, con fecha, hora, cupo, guía e
 * idioma propios. Sobre ella recaen la disponibilidad y las reservas.
 * <p>
 * Tabla: {@code salida_tour}. Una salida con reservas no se borra: se cancela.
 * <p>
 * Las reglas que necesitan consultar otras filas (cupo ocupado, cruces con
 * otras salidas del guía) se validan en {@code SalidaTourService}.
 */
@Entity
@Table(name = "salida_tour")
@Getter
@Setter
@NoArgsConstructor
public class SalidaTour {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSalida;

    /** Fecha de la salida. */
    @Column(nullable = false)
    private LocalDate fechaSalida;

    /** Hora de inicio. */
    @Column(nullable = false)
    private LocalTime horaSalida;

    /** Capacidad máxima de participantes; mayor que cero. */
    @Column(nullable = false)
    private Integer cupo;

    /** Estado de la salida. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private EstadoSalida estado = EstadoSalida.PROGRAMADA;

    /** Tour que se realiza. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tour")
    private Tour tour;

    /** Guía asignado. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_guia")
    private Guia guia;

    /** Idioma en que se ofrece la salida. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_idioma")
    private Idioma idioma;

    /**
     * @return fecha y hora en que empieza la salida
     */
    public LocalDateTime inicio() {
        return LocalDateTime.of(fechaSalida, horaSalida);
    }

    /**
     * @return fecha y hora en que termina: inicio más la duración del tour
     */
    public LocalDateTime fin() {
        return inicio().plusMinutes(tour.duracionEnMinutos());
    }

    /**
     * Indica si esta salida se cruza en el tiempo con otra. Dos salidas se
     * cruzan si una empieza antes de que la otra termine.
     *
     * @param otra salida a comparar (normalmente del mismo guía)
     * @return {@code true} si los intervalos se solapan
     */
    public boolean seCruzaCon(SalidaTour otra) {
        return this.inicio().isBefore(otra.fin()) && otra.inicio().isBefore(this.fin());
    }

    /**
     * Indica si el idioma de la salida lo habla el guía asignado.
     *
     * @return {@code true} si el guía habla el idioma de la salida
     */
    public boolean guiaHablaIdioma() {
        return guia != null && guia.hablaIdioma(idioma);
    }

    /**
     * Indica si la salida acepta nuevas reservas: debe estar programada y ser futura.
     *
     * @return {@code true} si está vigente
     */
    public boolean estaVigente() {
        return estado == EstadoSalida.PROGRAMADA && inicio().isAfter(LocalDateTime.now());
    }

    /**
     * @return {@code true} si la salida fue cancelada
     */
    public boolean estaCancelada() {
        return estado == EstadoSalida.CANCELADA;
    }

    /**
     * Cancela la salida. Las reservas y el reembolso del 100 % los procesa
     * {@code SalidaTourService}.
     *
     * @throws IllegalStateException si la salida ya se realizó
     */
    public void cancelar() {
        if (estado == EstadoSalida.REALIZADA) {
            throw new IllegalStateException("No se puede cancelar una salida ya realizada");
        }
        estado = EstadoSalida.CANCELADA;
    }

    /**
     * Marca la salida como realizada.
     *
     * @throws IllegalStateException si la salida está cancelada
     */
    public void marcarRealizada() {
        if (estado == EstadoSalida.CANCELADA) {
            throw new IllegalStateException("No se puede realizar una salida cancelada");
        }
        estado = EstadoSalida.REALIZADA;
    }
}
