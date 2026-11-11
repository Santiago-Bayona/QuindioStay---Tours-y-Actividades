package co.edu.uniquindio.QuindioStay_TyA.repository;

import co.edu.uniquindio.QuindioStay_TyA.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Acceso a datos de {@link Reserva}.
 * <p>
 * Los {@code Participante} no tienen repositorio propio: se guardan y se borran
 * en cascada con su reserva.
 */
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /**
     * Historial de reservas de un cliente, de la más reciente a la más antigua.
     *
     * @param idCliente id del cliente
     * @return reservas del cliente
     */
    @Query("SELECT r FROM Reserva r WHERE r.cliente.idCliente = :idCliente ORDER BY r.fechaCreacion DESC")
    List<Reserva> listarPorCliente(@Param("idCliente") Long idCliente);

    /**
     * Todas las reservas de una salida, incluidas las canceladas.
     *
     * @param idSalida id de la salida
     * @return reservas de la salida
     */
    @Query("SELECT r FROM Reserva r WHERE r.salida.idSalida = :idSalida ORDER BY r.idReserva")
    List<Reserva> listarPorSalida(@Param("idSalida") Long idSalida);

    /**
     * Reservas no canceladas de una salida. Las usa la cancelación por el
     * operador para cancelarlas y reembolsar el 100 %.
     *
     * @param idSalida id de la salida
     * @return reservas activas de la salida
     */
    @Query("""
            SELECT r FROM Reserva r
            WHERE r.salida.idSalida = :idSalida
              AND r.estado <> co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoReserva.CANCELADA
            ORDER BY r.idReserva
            """)
    List<Reserva> listarActivasPorSalida(@Param("idSalida") Long idSalida);

    /**
     * Indica si una persona ya está como participante en una reserva activa de
     * la salida. Regla: la misma persona no puede estar dos veces en la misma salida.
     *
     * @param idSalida  id de la salida
     * @param documento documento del participante
     * @return {@code true} si ya está registrada
     */
    @Query("""
            SELECT COUNT(p) > 0 FROM Participante p
            WHERE p.reserva.salida.idSalida = :idSalida
              AND p.documento = :documento
              AND p.reserva.estado <> co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoReserva.CANCELADA
            """)
    boolean existeParticipanteEnSalida(@Param("idSalida") Long idSalida,
                                       @Param("documento") String documento);

    /**
     * Indica si un plan ya se vendió. Un plan vendido no se borra, se inactiva.
     *
     * @param idPlan id del plan
     * @return {@code true} si tiene al menos una reserva
     */
    @Query("SELECT COUNT(r) > 0 FROM Reserva r WHERE r.plan.idPlan = :idPlan")
    boolean existePorPlan(@Param("idPlan") Long idPlan);

    /**
     * Indica si una salida tiene reservas. Una salida con reservas no se borra, se cancela.
     *
     * @param idSalida id de la salida
     * @return {@code true} si tiene al menos una reserva
     */
    @Query("SELECT COUNT(r) > 0 FROM Reserva r WHERE r.salida.idSalida = :idSalida")
    boolean existePorSalida(@Param("idSalida") Long idSalida);
}
