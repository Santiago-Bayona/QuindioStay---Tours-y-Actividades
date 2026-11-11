package co.edu.uniquindio.QuindioStay_TyA.repository;

import co.edu.uniquindio.QuindioStay_TyA.domain.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a datos de {@link Notificacion}.
 */
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /**
     * Notificaciones pendientes cuya fecha programada ya llegó. Las usa el
     * proceso de envío simulado (aprovecha el índice estado + fecha_programada).
     *
     * @param hasta momento actual
     * @return notificaciones por enviar, de la más antigua a la más reciente
     */
    @Query("""
            SELECT n FROM Notificacion n
            WHERE n.estado = co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoNotificacion.PENDIENTE
              AND n.fechaProgramada <= :hasta
            ORDER BY n.fechaProgramada
            """)
    List<Notificacion> listarPendientes(@Param("hasta") LocalDateTime hasta);

    /**
     * Historial de notificaciones de un cliente, de la más reciente a la más antigua.
     *
     * @param idCliente id del cliente
     * @return notificaciones del cliente
     */
    @Query("SELECT n FROM Notificacion n WHERE n.cliente.idCliente = :idCliente ORDER BY n.fechaProgramada DESC")
    List<Notificacion> listarPorCliente(@Param("idCliente") Long idCliente);
}
