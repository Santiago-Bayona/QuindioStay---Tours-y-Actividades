package co.edu.uniquindio.QuindioStay_TyA.domain;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.CanalNotificacion;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.IdiomaPreferencia;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Set;

/**
 * Cuenta y preferencias de un cliente para usar la plataforma en línea.
 * <p>
 * Tabla: {@code perfil_cliente}. Relación 1 a 1 con {@link Cliente}: su clave
 * primaria es la misma del cliente ({@code @MapsId}), por eso no se autogenera.
 */
@Entity
@Table(name = "perfil_cliente")
@Getter
@Setter
@NoArgsConstructor
public class PerfilCliente {

    /** Días de anticipación permitidos para los recordatorios. */
    public static final Set<Integer> ANTICIPACIONES_VALIDAS = Set.of(1, 3, 7);

    /** Mismo id del cliente dueño del perfil. */
    @Id
    private Long idCliente;

    /** Cliente dueño del perfil. */
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    /** Correo con el que inicia sesión. Único. */
    @Column(nullable = false, length = 254)
    private String correoCuenta;

    /** Contraseña cifrada con BCrypt; nunca en texto plano. */
    @Column(nullable = false, length = 255)
    private String contrasenaCifrada;

    /** Idioma preferido. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private IdiomaPreferencia idiomaPreferencia = IdiomaPreferencia.ESPANOL;

    /** Canal preferido para recibir notificaciones. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private CanalNotificacion canalNotificacionPreferido = CanalNotificacion.CORREO;

    /** Días antes del servicio en que se envía el recordatorio: 1, 3 o 7. */
    @Column(nullable = false)
    private Integer anticipacionRecordatorios = 1;

    /** Si el cliente acepta recibir promociones. */
    @Column(nullable = false)
    private Boolean aceptaPromociones = false;

    /**
     * Calcula la fecha en que debe enviarse el recordatorio de una reserva.
     *
     * @param fechaServicio fecha del servicio reservado
     * @return la fecha del servicio menos los días de anticipación del perfil
     */
    public LocalDate calcularFechaRecordatorio(LocalDate fechaServicio) {
        return fechaServicio.minusDays(anticipacionRecordatorios);
    }

    /**
     * Cambia los días de anticipación de los recordatorios.
     *
     * @param dias 1, 3 o 7
     * @throws IllegalArgumentException si el valor no es 1, 3 ni 7
     */
    public void cambiarAnticipacion(int dias) {
        if (!ANTICIPACIONES_VALIDAS.contains(dias)) {
            throw new IllegalArgumentException("La anticipación debe ser 1, 3 o 7 días");
        }
        this.anticipacionRecordatorios = dias;
    }

    /**
     * Indica si al cliente se le pueden enviar promociones.
     *
     * @return {@code true} solo si aceptó recibirlas
     */
    public boolean recibePromociones() {
        return Boolean.TRUE.equals(aceptaPromociones);
    }
}
