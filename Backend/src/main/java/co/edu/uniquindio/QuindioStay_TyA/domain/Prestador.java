package co.edu.uniquindio.QuindioStay_TyA.domain;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPrestador;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Empresa o persona que ofrece un servicio turístico (hotel, agencia, guía, etc.).
 * <p>
 * Tabla: {@code prestador}. Nunca se borra físicamente: cuando sale de la
 * plataforma pasa a estado {@link EstadoPrestador#RETIRADO} y conserva su historial.
 */
@Entity
@Table(name = "prestador")
@Getter
@Setter
@NoArgsConstructor
public class Prestador {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPrestador;

    /** Nombre comercial. */
    @Column(nullable = false, length = 120)
    private String nombreComercial;

    /** NIT o documento. Único. */
    @Column(nullable = false, length = 20)
    private String nitDocumento;

    /** Número del Registro Nacional de Turismo. Único. */
    @Column(nullable = false, length = 30)
    private String numeroRnt;

    /** Dirección física. */
    @Column(nullable = false, length = 200)
    private String direccion;

    /** Teléfono: 10 dígitos que empiezan por 3. */
    @Column(nullable = false, length = 20)
    private String telefono;

    /** Correo de contacto. Único. */
    @Column(nullable = false, length = 254)
    private String correo;

    /** Estado actual del prestador. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 15)
    private EstadoPrestador estado = EstadoPrestador.ACTIVO;

    /** Municipio donde se ubica. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_municipio")
    private Municipio municipio;

    /** Tipo de prestador (catálogo). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_prestador")
    private TipoPrestador tipoPrestador;

    /**
     * Indica si el prestador está activo. Solo un prestador activo puede
     * publicar tours y programar salidas.
     *
     * @return {@code true} si el estado es ACTIVO
     */
    public boolean estaActivo() {
        return estado == EstadoPrestador.ACTIVO;
    }

    /**
     * Suspende temporalmente al prestador.
     *
     * @throws IllegalStateException si el prestador ya está retirado
     */
    public void suspender() {
        if (estado == EstadoPrestador.RETIRADO) {
            throw new IllegalStateException("Un prestador retirado no se puede suspender");
        }
        estado = EstadoPrestador.SUSPENDIDO;
    }

    /**
     * Reactiva a un prestador suspendido.
     *
     * @throws IllegalStateException si el prestador está retirado
     */
    public void activar() {
        if (estado == EstadoPrestador.RETIRADO) {
            throw new IllegalStateException("Un prestador retirado no se puede reactivar");
        }
        estado = EstadoPrestador.ACTIVO;
    }

    /**
     * Retira al prestador de la plataforma. Reemplaza al borrado físico:
     * el registro y su historial se conservan.
     */
    public void retirar() {
        estado = EstadoPrestador.RETIRADO;
    }
}
