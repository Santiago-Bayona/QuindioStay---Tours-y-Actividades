package co.edu.uniquindio.QuindioStay_TyA.domain;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoUsuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Usuario interno que administra la plataforma (administrador, encargado de
 * prestador u operador). Es distinto de un cliente.
 * <p>
 * Tabla: {@code usuario_sistema}. El encargado de prestador debe tener un
 * prestador asociado y solo gestiona la información de ese prestador.
 */
@Entity
@Table(name = "usuario_sistema")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioSistema {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    /** Nombre de usuario para iniciar sesión. Único. */
    @Column(nullable = false, length = 80)
    private String nombreUsuario;

    /** Contraseña cifrada con BCrypt; nunca en texto plano. */
    @Column(nullable = false, length = 255)
    private String contrasenaCifrada;

    /** Estado del usuario. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 15)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    /** Rol del usuario. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rol")
    private Rol rol;

    /** Prestador que administra; {@code null} para el administrador y el operador. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prestador")
    private Prestador prestador;

    /**
     * Indica si el usuario puede iniciar sesión.
     *
     * @return {@code true} si está activo
     */
    public boolean estaActivo() {
        return estado == EstadoUsuario.ACTIVO;
    }

    /** Bloquea al usuario; ya no podrá iniciar sesión. */
    public void bloquear() {
        estado = EstadoUsuario.BLOQUEADO;
    }

    /** Desbloquea al usuario. */
    public void desbloquear() {
        estado = EstadoUsuario.ACTIVO;
    }

    /** @return {@code true} si el rol es administrador de la plataforma */
    public boolean esAdministrador() {
        return rol != null && rol.esRol(Rol.ADMINISTRADOR);
    }

    /** @return {@code true} si el rol es encargado de prestador */
    public boolean esEncargadoPrestador() {
        return rol != null && rol.esRol(Rol.ENCARGADO_PRESTADOR);
    }

    /** @return {@code true} si el rol es operador */
    public boolean esOperador() {
        return rol != null && rol.esRol(Rol.OPERADOR);
    }

    /**
     * Verifica la regla "un encargado de prestador debe tener prestador asociado".
     *
     * @return {@code true} si la combinación de rol y prestador es válida
     */
    public boolean tienePrestadorValido() {
        return !esEncargadoPrestador() || prestador != null;
    }

    /**
     * Indica si el usuario puede gestionar la información de un prestador.
     * El administrador gestiona todos; el encargado solo el suyo.
     *
     * @param idPrestador id del prestador que se quiere gestionar
     * @return {@code true} si tiene permiso
     */
    public boolean puedeGestionarPrestador(Long idPrestador) {
        if (esAdministrador()) {
            return true;
        }
        return esEncargadoPrestador()
                && prestador != null
                && prestador.getIdPrestador().equals(idPrestador);
    }
}
