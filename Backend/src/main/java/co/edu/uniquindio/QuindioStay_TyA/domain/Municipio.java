package co.edu.uniquindio.QuindioStay_TyA.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Municipio del Quindío donde se ubican los prestadores.
 * <p>
 * Tabla: {@code municipio}. La plataforma trabaja con los 12 municipios del
 * departamento; el nombre y el código DANE no se repiten.
 */
@Entity
@Table(name = "municipio")
@Getter
@Setter
@NoArgsConstructor
public class Municipio {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMunicipio;

    /** Nombre del municipio. Único. */
    @Column(nullable = false, length = 100)
    private String nombre;

    /** Código DANE de 5 dígitos. Único. */
    @Column(nullable = false, length = 5)
    private String codigoDane;

    /**
     * Crea un municipio nuevo (sin id, lo asigna la base de datos).
     *
     * @param nombre     nombre del municipio
     * @param codigoDane código DANE de 5 dígitos
     */
    public Municipio(String nombre, String codigoDane) {
        this.nombre = nombre;
        this.codigoDane = codigoDane;
    }
}
