package co.edu.uniquindio.QuindioStay_TyA.repository;

import co.edu.uniquindio.QuindioStay_TyA.domain.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de {@link Pago}. El historial de pagos nunca se borra.
 */
public interface PagoRepository extends JpaRepository<Pago, Long> {

    /**
     * Pagos de una reserva en orden cronológico (abonos, saldos, fallidos y reembolsos).
     *
     * @param idReserva id de la reserva
     * @return pagos de la reserva
     */
    @Query("SELECT p FROM Pago p WHERE p.reserva.idReserva = :idReserva ORDER BY p.fecha")
    List<Pago> listarPorReserva(@Param("idReserva") Long idReserva);

    /**
     * Suma de los pagos exitosos de una reserva. Si no hay pagos devuelve 0.
     * Con este valor se decide si la reserva está pagada.
     *
     * @param idReserva id de la reserva
     * @return total pagado con éxito
     */
    @Query("""
            SELECT COALESCE(SUM(p.monto), 0) FROM Pago p
            WHERE p.reserva.idReserva = :idReserva
              AND p.estado = co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPago.EXITOSO
            """)
    BigDecimal sumarExitososPorReserva(@Param("idReserva") Long idReserva);

    /**
     * Busca un pago por la referencia de la pasarela, para conciliar.
     *
     * @param referencia referencia de transacción
     * @return el pago, o vacío si no existe
     */
    @Query("SELECT p FROM Pago p WHERE p.referenciaTransaccion = :referencia")
    Optional<Pago> buscarPorReferencia(@Param("referencia") String referencia);
}
