package co.edu.uniquindio.QuindioStay_TyA.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Catálogo de idiomas de la plataforma: español, inglés, francés, alemán, portugués.
 * <p>
 * Tabla: {@code idioma}. Lo usan los guías (idiomas que hablan) y las salidas
 * (idioma en que se ofrecen).
 */
@Entity
@Table(name = "idioma")
@Getter
@Setter
@NoArgsConstructor
public class Idioma {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idIdioma;

    /** Nombre del idioma. Único. */
    @Column(nullable = false, length = 40)
    private String nombreIdioma;

    /**
     * Crea un idioma nuevo.
     *
     * @param nombreIdioma nombre del idioma
     */
    public Idioma(String nombreIdioma) {
        this.nombreIdioma = nombreIdioma;
    }

    /**
     * Compara dos idiomas por su id. Se usa en lugar de {@code equals} porque
     * las entidades JPA pueden llegar como proxies distintos del mismo registro.
     *
     * @param otro idioma a comparar
     * @return {@code true} si ambos tienen el mismo id
     */
    public boolean esMismoIdioma(Idioma otro) {
        return otro != null && idIdioma != null && idIdioma.equals(otro.getIdIdioma());
    }
}
