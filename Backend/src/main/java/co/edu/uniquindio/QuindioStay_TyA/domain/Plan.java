package co.edu.uniquindio.QuindioStay_TyA.domain;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPlan;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.TipoPlan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Forma en que un prestador ofrece su servicio (grupal, privado, todo incluido...).
 * <p>
 * Tabla: {@code plan}. Un plan que ya se vendió no se borra: se inactiva.
 * Dos planes del mismo prestador no pueden llamarse igual.
 */
@Entity
@Table(name = "plan")
@Getter
@Setter
@NoArgsConstructor
public class Plan {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPlan;

    /** Nombre del plan; único por prestador. */
    @Column(nullable = false, length = 120)
    private String nombre;

    /** Descripción del plan. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    /** Tipo de plan. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private TipoPlan tipo;

    /** Precio del plan; mayor que cero. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    /** Inicio de la vigencia. */
    @Column(nullable = false)
    private LocalDate fechaInicio;

    /** Fin de la vigencia; posterior a la fecha de inicio. */
    @Column(nullable = false)
    private LocalDate fechaFin;

    /** Estado del plan. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 15)
    private EstadoPlan estado = EstadoPlan.ACTIVO;

    /** Prestador que ofrece el plan. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_prestador")
    private Prestador prestador;

    /**
     * Indica si el plan se puede reservar para una fecha de servicio.
     *
     * @param fecha fecha del servicio
     * @return {@code true} si el plan está activo y la fecha está dentro de su vigencia
     */
    public boolean estaVigente(LocalDate fecha) {
        return estado == EstadoPlan.ACTIVO
                && !fecha.isBefore(fechaInicio)
                && !fecha.isAfter(fechaFin);
    }

    /**
     * Inactiva el plan. Reemplaza al borrado cuando el plan ya tiene reservas.
     */
    public void inactivar() {
        estado = EstadoPlan.INACTIVO;
    }

    /**
     * Vuelve a activar el plan.
     */
    public void activar() {
        estado = EstadoPlan.ACTIVO;
    }

    /**
     * Indica si el plan pertenece a un prestador dado. Sirve para validar que
     * el plan de una reserva sea del mismo prestador que publicó el tour.
     *
     * @param otro prestador a comparar
     * @return {@code true} si es el mismo prestador (comparado por id)
     */
    public boolean perteneceA(Prestador otro) {
        return otro != null && prestador != null
                && prestador.getIdPrestador().equals(otro.getIdPrestador());
    }
}
