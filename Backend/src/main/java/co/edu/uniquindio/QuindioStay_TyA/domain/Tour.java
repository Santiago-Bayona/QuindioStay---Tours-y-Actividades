package co.edu.uniquindio.QuindioStay_TyA.domain;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.Dificultad;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

/**
 * Experiencia turística que publica un operador, por ejemplo la caminata al
 * Valle de Cocora. Describe lo que no cambia entre una salida y otra.
 * <p>
 * Tabla: {@code tour}. Un prestador no publica dos tours con el mismo nombre.
 */
@Entity
@Table(name = "tour")
@Getter
@Setter
@NoArgsConstructor
public class Tour {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTour;

    /** Nombre del tour; único por prestador. */
    @Column(nullable = false, length = 120)
    private String nombre;

    /** Descripción de la actividad y lo que incluye. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    /** Duración en horas; mayor que cero. */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal duracionHoras;

    /** Nivel de dificultad. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Dificultad dificultad;

    /** Edad mínima para participar; 0 significa sin restricción. */
    @Column(nullable = false)
    private Integer edadMinima = 0;

    /** Precio por persona; mayor que cero. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioPersona;

    /** Categoría del tour. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria")
    private CategoriaTour categoria;

    /** Prestador (operador) que publica el tour. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_prestador")
    private Prestador prestador;

    /**
     * Indica si una persona de cierta edad puede participar en el tour.
     *
     * @param edad edad de la persona en la fecha de la salida
     * @return {@code true} si la edad es mayor o igual a la edad mínima
     */
    public boolean permiteEdad(int edad) {
        return edad >= edadMinima;
    }

    /**
     * Convierte la duración del tour a minutos, por ejemplo 3.5 horas = 210 minutos.
     *
     * @return duración en minutos
     */
    public long duracionEnMinutos() {
        return duracionHoras.multiply(BigDecimal.valueOf(60)).longValue();
    }
}
