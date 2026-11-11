package co.edu.uniquindio.QuindioStay_TyA.domain;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.CanalNotificacion;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoNotificacion;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.TipoNotificacion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Mensaje que la plataforma envía a un cliente: recordatorio, confirmación,
 * cambio de estado o promoción. El envío es simulado.
 * <p>
 * Tabla: {@code notificacion}. Puede o no estar ligada a una reserva:
 * las promociones nunca lo están y los recordatorios siempre.
 */
@Entity
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
public class Notificacion {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotificacion;

    /** Tipo de notificación. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 40)
    private TipoNotificacion tipo;

    /** Canal de envío. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private CanalNotificacion canal;

    /** Contenido del mensaje. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    /** Fecha y hora prevista para el envío. */
    @Column(nullable = false)
    private LocalDateTime fechaProgramada;

    /** Fecha y hora real del envío; {@code null} mientras no se envíe. */
    private LocalDateTime fechaEnvio;

    /** Estado de la notificación. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 15)
    private EstadoNotificacion estado = EstadoNotificacion.PENDIENTE;

    /** Cliente destinatario. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    /** Reserva relacionada; {@code null} en promociones. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    /**
     * Crea una notificación pendiente.
     *
     * @param tipo            tipo de notificación
     * @param canal           canal de envío
     * @param mensaje         contenido
     * @param fechaProgramada cuándo debe enviarse
     * @param cliente         destinatario
     * @param reserva         reserva relacionada; {@code null} para promociones
     * @throws IllegalArgumentException si la combinación de tipo y reserva no es válida
     */
    public Notificacion(TipoNotificacion tipo, CanalNotificacion canal, String mensaje,
                        LocalDateTime fechaProgramada, Cliente cliente, Reserva reserva) {
        this.tipo = tipo;
        this.canal = canal;
        this.mensaje = mensaje;
        this.fechaProgramada = fechaProgramada;
        this.cliente = cliente;
        this.reserva = reserva;
        validarReserva();
    }

    /**
     * Verifica que una promoción no tenga reserva y que un recordatorio sí la tenga.
     * Esta regla no se puede poner como CHECK en MySQL porque la FK usa SET NULL.
     *
     * @throws IllegalArgumentException si no se cumple
     */
    public void validarReserva() {
        if (tipo == TipoNotificacion.PROMOCION && reserva != null) {
            throw new IllegalArgumentException("Una promoción no se liga a ninguna reserva");
        }
        if (tipo == TipoNotificacion.RECORDATORIO_RESERVA && reserva == null) {
            throw new IllegalArgumentException("Un recordatorio debe tener una reserva");
        }
    }

    /**
     * @return {@code true} si es una promoción
     */
    public boolean esPromocion() {
        return tipo == TipoNotificacion.PROMOCION;
    }

    /**
     * Indica si la notificación ya debe enviarse: está pendiente y su fecha
     * programada ya llegó. La usa el proceso de envío simulado.
     *
     * @param ahora momento actual
     * @return {@code true} si debe enviarse
     */
    public boolean debeEnviarse(LocalDateTime ahora) {
        return estado == EstadoNotificacion.PENDIENTE && !fechaProgramada.isAfter(ahora);
    }

    /**
     * Marca la notificación como enviada.
     *
     * @param fecha fecha y hora del envío
     * @throws IllegalArgumentException si la fecha es anterior a la programada
     */
    public void marcarEnviada(LocalDateTime fecha) {
        if (fecha.isBefore(fechaProgramada)) {
            throw new IllegalArgumentException("La fecha de envío no puede ser anterior a la programada");
        }
        fechaEnvio = fecha;
        estado = EstadoNotificacion.ENVIADA;
    }

    /** Marca la notificación como fallida. */
    public void marcarFallida() {
        estado = EstadoNotificacion.FALLIDA;
    }

    /**
     * Marca la notificación como leída.
     *
     * @throws IllegalStateException si no había sido enviada
     */
    public void marcarLeida() {
        if (estado != EstadoNotificacion.ENVIADA) {
            throw new IllegalStateException("Solo se puede leer una notificación enviada");
        }
        estado = EstadoNotificacion.LEIDA;
    }
}
