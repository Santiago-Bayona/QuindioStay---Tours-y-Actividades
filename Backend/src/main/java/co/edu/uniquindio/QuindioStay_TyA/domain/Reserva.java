package co.edu.uniquindio.QuindioStay_TyA.domain;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.CanalReserva;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoReserva;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro que une a un cliente con la salida de tour que reservó.
 * <p>
 * Tabla: {@code reserva}. Una reserva cancelada no se borra: queda como historial.
 * Sus participantes se guardan en cascada con ella.
 * <p>
 * Las validaciones que dependen de otras reservas (cupo disponible, participante
 * repetido en la misma salida) se hacen en {@code ReservaService}.
 */
@Entity
@Table(name = "reserva")
@Getter
@Setter
@NoArgsConstructor
public class Reserva {

    /** Días de anticipación a partir de los cuales el cliente recibe reembolso. */
    public static final int DIAS_MINIMOS_REEMBOLSO = 5;
    /** Porcentaje que se devuelve cuando el cliente cancela a tiempo. */
    public static final BigDecimal PORCENTAJE_REEMBOLSO_CLIENTE = new BigDecimal("0.80");

    /** Identificador autogenerado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReserva;

    /** Fecha y hora de creación; se asigna al guardar si viene vacía. */
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    /** Fecha del servicio; debe coincidir con la fecha de la salida. */
    @Column(nullable = false)
    private LocalDate fechaServicio;

    /** Estado de la reserva. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 15)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    /** Valor total: precio por persona del tour por número de participantes. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    /** Fecha y hora de cancelación; solo tiene valor si está cancelada. */
    private LocalDateTime fechaCancelacion;

    /** Canal por el que se hizo la reserva. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 25)
    private CanalReserva canal;

    /** Cliente que reserva. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    /** Plan elegido; debe ser del mismo prestador que el tour. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_plan")
    private Plan plan;

    /** Salida reservada. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_salida")
    private SalidaTour salida;

    /** Personas que asisten. Se guardan y se borran junto con la reserva. */
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participante> participantes = new ArrayList<>();

    /** Pagos asociados (abonos, saldos, fallidos y reembolsos). */
    @OneToMany(mappedBy = "reserva", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Pago> pagos = new ArrayList<>();

    /** Asigna la fecha de creación antes de insertar si no se definió. */
    @PrePersist
    protected void alCrear() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    // ------------------------------------------------------------------
    // Participantes
    // ------------------------------------------------------------------

    /**
     * Agrega un participante a la reserva y le asigna el siguiente número
     * consecutivo dentro de ella (1, 2, 3...).
     *
     * @param participante participante a agregar
     */
    public void agregarParticipante(Participante participante) {
        int siguiente = participantes.stream()
                .mapToInt(Participante::getNumParticipante)
                .max()
                .orElse(0) + 1;
        participante.asignarAReserva(this, siguiente);
        participantes.add(participante);
    }

    /**
     * Quita un participante de la reserva; se borra de la base al guardar.
     *
     * @param participante participante a quitar
     */
    public void quitarParticipante(Participante participante) {
        participantes.remove(participante);
    }

    /**
     * @return número de personas que asisten (lo que ocupa del cupo de la salida)
     */
    public int cantidadParticipantes() {
        return participantes.size();
    }

    /**
     * Indica si existe un participante con el documento dado en esta reserva.
     *
     * @param documento documento a buscar
     * @return {@code true} si ya está registrado
     */
    public boolean tieneParticipante(String documento) {
        return participantes.stream().anyMatch(p -> p.getDocumento().equals(documento));
    }

    // ------------------------------------------------------------------
    // Valor y pagos
    // ------------------------------------------------------------------

    /**
     * Calcula el valor total: precio por persona del tour por el número de participantes.
     *
     * @return valor calculado
     */
    public BigDecimal calcularValorTotal() {
        BigDecimal precio = salida.getTour().getPrecioPersona();
        return precio.multiply(BigDecimal.valueOf(cantidadParticipantes()));
    }

    /**
     * Calcula y guarda el valor total. Se llama al crear la reserva para que un
     * cambio posterior en el precio del tour no altere reservas ya hechas.
     */
    public void actualizarValorTotal() {
        this.valorTotal = calcularValorTotal();
    }

    /**
     * Asocia un pago a la reserva manteniendo la relación en ambos lados.
     *
     * @param pago pago a registrar
     */
    public void agregarPago(Pago pago) {
        pago.setReserva(this);
        pagos.add(pago);
    }

    /**
     * @return suma de los pagos con estado EXITOSO
     */
    public BigDecimal totalPagadoExitoso() {
        return pagos.stream()
                .filter(Pago::esExitoso)
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Una reserva está pagada cuando sus pagos exitosos cubren el valor total.
     *
     * @return {@code true} si está pagada
     */
    public boolean estaPagada() {
        return totalPagadoExitoso().compareTo(valorTotal) >= 0;
    }

    /**
     * @return lo que falta por pagar; cero si ya está pagada
     */
    public BigDecimal saldoPendiente() {
        BigDecimal saldo = valorTotal.subtract(totalPagadoExitoso());
        return saldo.max(BigDecimal.ZERO);
    }

    // ------------------------------------------------------------------
    // Cancelación y reembolsos
    // ------------------------------------------------------------------

    /**
     * Calcula los días entre la cancelación y la fecha del servicio.
     *
     * @return días de anticipación con que se canceló
     * @throws IllegalStateException si la reserva no está cancelada
     */
    public long diasDeAnticipacion() {
        if (fechaCancelacion == null) {
            throw new IllegalStateException("La reserva no tiene fecha de cancelación");
        }
        return ChronoUnit.DAYS.between(fechaCancelacion.toLocalDate(), fechaServicio);
    }

    /**
     * Regla del núcleo: si el cliente cancela con más de 5 días de anticipación
     * se le devuelve el 80 % de lo pagado; con 5 días o menos, nada.
     *
     * @return monto a reembolsar
     */
    public BigDecimal calcularReembolsoCliente() {
        if (diasDeAnticipacion() > DIAS_MINIMOS_REEMBOLSO) {
            return totalPagadoExitoso()
                    .multiply(PORCENTAJE_REEMBOLSO_CLIENTE)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Regla del dominio: si el operador cancela la salida se devuelve el 100 %
     * de lo pagado, sin importar la anticipación.
     *
     * @return monto a reembolsar
     */
    public BigDecimal calcularReembolsoOperador() {
        return totalPagadoExitoso();
    }

    // ------------------------------------------------------------------
    // Cambios de estado
    // ------------------------------------------------------------------

    /**
     * @return {@code true} si la reserva no está cancelada (cuenta para el cupo)
     */
    public boolean estaActiva() {
        return estado != EstadoReserva.CANCELADA;
    }

    /**
     * Confirma la reserva.
     *
     * @throws IllegalStateException si no está pendiente
     */
    public void confirmar() {
        if (estado != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException("Solo se puede confirmar una reserva pendiente");
        }
        estado = EstadoReserva.CONFIRMADA;
    }

    /**
     * Cancela la reserva y registra la fecha de cancelación (ambos datos van
     * juntos por la restricción de la tabla). No calcula el reembolso.
     *
     * @param fecha fecha y hora de la cancelación
     * @throws IllegalStateException si ya está cancelada o completada
     */
    public void cancelar(LocalDateTime fecha) {
        if (estado == EstadoReserva.CANCELADA || estado == EstadoReserva.COMPLETADA) {
            throw new IllegalStateException("La reserva ya está " + estado.name().toLowerCase());
        }
        estado = EstadoReserva.CANCELADA;
        fechaCancelacion = fecha;
    }

    /**
     * Marca la reserva como completada.
     *
     * @throws IllegalStateException si no estaba confirmada
     */
    public void completar() {
        if (estado != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo se completa una reserva confirmada");
        }
        estado = EstadoReserva.COMPLETADA;
    }
}
