package co.edu.uniquindio.QuindioStay_TyA.domain;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.TipoDocumento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

/**
 * Persona que accede a los servicios de la plataforma y realiza reservas.
 * <p>
 * Tabla: {@code cliente}. El par (tipo de documento, número) y el correo son
 * únicos. Un cliente registrado en el mostrador puede no tener perfil.
 */
@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;

    /** Nombres. */
    @Column(nullable = false, length = 80)
    private String nombre;

    /** Apellidos. */
    @Column(nullable = false, length = 80)
    private String apellido;

    /** Tipo de documento de identidad. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private TipoDocumento tipoDocumento;

    /** Número de documento. Único junto con el tipo. */
    @Column(nullable = false, length = 30)
    private String numeroDocumento;

    /** Correo de contacto. Único. */
    @Column(nullable = false, length = 254)
    private String correo;

    /** Teléfono: 10 dígitos que empiezan por 3. */
    @Column(nullable = false, length = 20)
    private String telefono;

    /** Fecha de nacimiento; no puede ser futura (se valida en el servicio). */
    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    /** Ciudad de origen. */
    @Column(nullable = false, length = 100)
    private String ciudadOrigen;

    /** Perfil con cuenta y preferencias. Puede ser {@code null}. */
    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private PerfilCliente perfil;

    /**
     * Indica si el cliente tiene cuenta y preferencias registradas.
     *
     * @return {@code true} si tiene perfil
     */
    public boolean tienePerfil() {
        return perfil != null;
    }

    /**
     * Asigna el perfil al cliente manteniendo la relación en ambos lados.
     * Úsese siempre este método en lugar de {@code setPerfil}.
     *
     * @param perfil perfil a asociar
     */
    public void asignarPerfil(PerfilCliente perfil) {
        this.perfil = perfil;
        if (perfil != null) {
            perfil.setCliente(this);
        }
    }

    /**
     * Devuelve el nombre completo para comprobantes, facturas y reportes.
     *
     * @return nombre y apellido separados por un espacio
     */
    public String nombreCompleto() {
        return nombre + " " + apellido;
    }
}
