package co.edu.uniquindio.QuindioStay_TyA.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Catálogo de categorías de tour: cultural, aventura, naturaleza, cafetero.
 * <p>
 * Tabla: {@code categoria_tour}. Permite al viajero filtrar el catálogo.
 */
@Entity
@Table(name = "categoria_tour")
@Getter
@Setter
@NoArgsConstructor
public class CategoriaTour {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategoria;

    /** Nombre de la categoría. Único. */
    @Column(nullable = false, length = 80)
    private String nombre;

    /**
     * Crea una categoría nueva.
     *
     * @param nombre nombre de la categoría
     */
    public CategoriaTour(String nombre) {
        this.nombre = nombre;
    }
}
