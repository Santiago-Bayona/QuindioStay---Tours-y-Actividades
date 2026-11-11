package co.edu.uniquindio.QuindioStay_TyA.domain;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPago;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.MetodoPago;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Dinero que un cliente entrega (o recibe de vuelta) por una reserva.
 * <p>
 * Tabla: {@code pago}. Una reserva puede tener varios pagos (abono y saldo).
 * Los reembolsos se registran como un pago con estado REEMBOLSADO.
 * El historial de pagos nunca se borra.
 */
@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
public class Pago {

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPago;

    /** Fecha y hora del pago; se asigna al guardar si viene vacía. */
    @Column(nullable = false)
    private LocalDateTime fecha;

    /** Monto; estrictamente mayor que cero. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    /** Método de pago. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private MetodoPago metodo;

    /** Estado del pago. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    /** Referencia de la pasarela; obligatoria en pagos en línea. Única. */
    @Column(length = 100)
    private String referenciaTransaccion;

    /** Reserva a la que corresponde el pago. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    /**
     * Crea un pago nuevo en estado pendiente.
     *
     * @param monto                 monto del pago
     * @param metodo                método de pago
     * @param referenciaTransaccion referencia; {@code null} en efectivo o transferencia
     */
    public Pago(BigDecimal monto, MetodoPago metodo, String referenciaTransaccion) {
        this.monto = monto;
        this.metodo = metodo;
        this.referenciaTransaccion = referenciaTransaccion;
    }

    /**
     * Crea el registro de un reembolso.
     *
     * @param monto      monto devuelto
     * @param metodo     método por el que se devuelve
     * @param referencia referencia de la devolución; puede ser {@code null}
     * @return pago en estado REEMBOLSADO, listo para agregarse a la reserva
     */
    public static Pago crearReembolso(BigDecimal monto, MetodoPago metodo, String referencia) {
        Pago reembolso = new Pago(monto, metodo, referencia);
        reembolso.estado = EstadoPago.REEMBOLSADO;
        return reembolso;
    }

    /** Asigna la fecha y valida la referencia antes de insertar o actualizar. */
    @PrePersist
    @PreUpdate
    protected void antesDeGuardar() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        validarReferencia();
    }

    /**
     * @return {@code true} si el dinero fue recibido
     */
    public boolean esExitoso() {
        return estado == EstadoPago.EXITOSO;
    }

    /**
     * Los pagos en línea (tarjeta y PSE) exigen referencia de transacción.
     *
     * @return {@code true} si el método requiere referencia
     */
    public boolean requiereReferencia() {
        return metodo != null && metodo.esEnLinea();
    }

    /**
     * Verifica la misma regla del CHECK de la tabla para dar un error claro
     * antes de llegar a la base de datos.
     *
     * @throws IllegalStateException si falta la referencia en un pago en línea
     */
    public void validarReferencia() {
        if (requiereReferencia() && (referenciaTransaccion == null || referenciaTransaccion.isBlank())) {
            throw new IllegalStateException("Los pagos con " + metodo + " requieren referencia de transacción");
        }
    }

    /** Marca el pago como exitoso. */
    public void marcarExitoso() {
        estado = EstadoPago.EXITOSO;
    }

    /** Marca el pago como fallido. */
    public void marcarFallido() {
        estado = EstadoPago.FALLIDO;
    }
}
