package co.edu.uniquindio.QuindioStay_TyA.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Guía turístico que acompaña y dirige las salidas.
 * <p>
 * Tabla: {@code guia}. Los idiomas que habla se guardan en la tabla asociativa
 * {@code guia_idioma}, mapeada aquí con {@code @ManyToMany}; por eso no existe
 * una clase GuiaIdioma.
 */
@Entity
@Table(name = "guia")
@Getter
@Setter
@NoArgsConstructor
public class Guia {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGuia;

    /** Nombre completo. */
    @Column(nullable = false, length = 120)
    private String nombre;

    /** Documento de identidad. Único. */
    @Column(nullable = false, length = 30)
    private String documento;

    /** Número de tarjeta profesional. Único. */
    @Column(nullable = false, length = 40)
    private String tarjetaProfesional;

    /** Idiomas que habla el guía (tabla {@code guia_idioma}). */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "guia_idioma",
            joinColumns = @JoinColumn(name = "id_guia"),
            inverseJoinColumns = @JoinColumn(name = "id_idioma"))
    private Set<Idioma> idiomas = new HashSet<>();

    /**
     * Crea un guía nuevo sin idiomas.
     *
     * @param nombre             nombre completo
     * @param documento          documento de identidad
     * @param tarjetaProfesional número de tarjeta profesional
     */
    public Guia(String nombre, String documento, String tarjetaProfesional) {
        this.nombre = nombre;
        this.documento = documento;
        this.tarjetaProfesional = tarjetaProfesional;
    }

    /**
     * Indica si el guía habla un idioma. Se usa para validar que el guía
     * asignado a una salida hable el idioma en que se ofrece.
     *
     * @param idioma idioma a verificar
     * @return {@code true} si el idioma está entre los registrados
     */
    public boolean hablaIdioma(Idioma idioma) {
        return idiomas.stream().anyMatch(i -> i.esMismoIdioma(idioma));
    }

    /**
     * Registra un idioma que habla el guía. Si ya lo tenía, no hace nada.
     *
     * @param idioma idioma a agregar
     */
    public void agregarIdioma(Idioma idioma) {
        if (!hablaIdioma(idioma)) {
            idiomas.add(idioma);
        }
    }

    /**
     * Quita un idioma del guía.
     *
     * @param idioma idioma a quitar
     */
    public void quitarIdioma(Idioma idioma) {
        idiomas.removeIf(i -> i.esMismoIdioma(idioma));
    }

    /**
     * Indica si el guía tiene al menos un idioma. Un guía sin idiomas no se
     * puede asignar a ninguna salida.
     *
     * @return {@code true} si tiene uno o más idiomas
     */
    public boolean tieneIdiomas() {
        return !idiomas.isEmpty();
    }
}
