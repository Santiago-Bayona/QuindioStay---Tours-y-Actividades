package co.edu.uniquindio.QuindioStay_TyA.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Catálogo de roles de los usuarios internos del sistema.
 * <p>
 * Tabla: {@code rol}. Del rol depende qué puede ver y hacer cada usuario.
 */
@Entity
@Table(name = "rol")
@Getter
@Setter
@NoArgsConstructor
public class Rol {

    /** Nombre del rol administrador, tal como está guardado en la base de datos. */
    public static final String ADMINISTRADOR = "Administrador de la plataforma";
    /** Nombre del rol encargado de prestador, tal como está guardado en la base de datos. */
    public static final String ENCARGADO_PRESTADOR = "Encargado de prestador";
    /** Nombre del rol operador, tal como está guardado en la base de datos. */
    public static final String OPERADOR = "Operador";

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRol;

    /** Nombre del rol. Único. */
    @Column(nullable = false, length = 40)
    private String nombre;

    /**
     * Crea un rol nuevo.
     *
     * @param nombre nombre del rol
     */
    public Rol(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Indica si este rol tiene el nombre dado (sin importar mayúsculas).
     *
     * @param nombreRol nombre a comparar, por ejemplo {@link #ADMINISTRADOR}
     * @return {@code true} si coinciden
     */
    public boolean esRol(String nombreRol) {
        return nombre != null && nombre.equalsIgnoreCase(nombreRol);
    }
}
