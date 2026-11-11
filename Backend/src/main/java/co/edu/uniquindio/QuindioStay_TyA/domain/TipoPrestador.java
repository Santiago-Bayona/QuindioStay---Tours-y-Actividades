package co.edu.uniquindio.QuindioStay_TyA.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Catálogo de tipos de prestador: alojamiento, agencia de viajes, transporte,
 * gastronomía, guianza, alquiler de vehículos, atracción u otros.
 * <p>
 * Tabla: {@code tipo_prestador}. Es una entidad y no un enum porque el
 * enunciado permite agregar nuevos tipos sin cambiar el código.
 */
@Entity
@Table(name = "tipo_prestador")
@Getter
@Setter
@NoArgsConstructor
public class TipoPrestador {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTipoPrestador;

    /** Nombre del tipo. Único. */
    @Column(nullable = false, length = 60)
    private String nombreTipo;

    /**
     * Crea un tipo de prestador nuevo.
     *
     * @param nombreTipo nombre del tipo
     */
    public TipoPrestador(String nombreTipo) {
        this.nombreTipo = nombreTipo;
    }
}
