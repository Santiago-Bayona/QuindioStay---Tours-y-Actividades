package co.edu.uniquindio.QuindioStay_TyA.repository;

import co.edu.uniquindio.QuindioStay_TyA.domain.SalidaTour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a datos de {@link SalidaTour}. Aquí están las dos consultas clave de
 * las reglas del dominio: el cupo ocupado y el cruce de horarios del guía.
 */
public interface SalidaTourRepository extends JpaRepository<SalidaTour, Long> {

    /**
     * Lista las salidas de un tour, de la más próxima a la más lejana.
     *
     * @param idTour id del tour
     * @return salidas del tour
     */
    @Query("SELECT s FROM SalidaTour s WHERE s.tour.idTour = :idTour ORDER BY s.fechaSalida, s.horaSalida")
    List<SalidaTour> listarPorTour(@Param("idTour") Long idTour);

    /**
     * Lista las salidas asignadas a un guía.
     *
     * @param idGuia id del guía
     * @return salidas del guía
     */
    @Query("SELECT s FROM SalidaTour s WHERE s.guia.idGuia = :idGuia ORDER BY s.fechaSalida, s.horaSalida")
    List<SalidaTour> listarPorGuia(@Param("idGuia") Long idGuia);

    /**
     * Cuenta los participantes de las reservas no canceladas de una salida.
     * Cupo disponible = cupo de la salida - este valor.
     *
     * @param idSalida id de la salida
     * @return número de cupos ocupados
     */
    @Query("""
            SELECT COUNT(p) FROM Participante p
            WHERE p.reserva.salida.idSalida = :idSalida
              AND p.reserva.estado <> co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoReserva.CANCELADA
            """)
    long contarCuposOcupados(@Param("idSalida") Long idSalida);

    /**
     * Cuenta las salidas no canceladas del guía que se cruzan con el intervalo
     * [inicio, fin). Cada salida ocupa desde su hora hasta hora + duración del tour.
     * Es SQL nativo porque JPQL no permite sumar la duración a una fecha.
     *
     * @param idGuia          id del guía
     * @param inicio          inicio de la salida nueva
     * @param fin             fin de la salida nueva
     * @param idSalidaExcluir salida a ignorar (al editar una existente); {@code null} al crear
     * @return número de salidas que se cruzan
     */
    @Query(value = """
            SELECT COUNT(*) FROM salida_tour s
            JOIN tour t ON t.id_tour = s.id_tour
            WHERE s.id_guia = :idGuia
              AND s.estado <> 'CANCELADA'
              AND (:idSalidaExcluir IS NULL OR s.id_salida <> :idSalidaExcluir)
              AND TIMESTAMP(s.fecha_salida, s.hora_salida) < :fin
              AND TIMESTAMP(s.fecha_salida, s.hora_salida)
                  + INTERVAL ROUND(t.duracion_horas * 60) MINUTE > :inicio
            """, nativeQuery = true)
    long contarCrucesDelGuia(@Param("idGuia") Long idGuia,
                             @Param("inicio") LocalDateTime inicio,
                             @Param("fin") LocalDateTime fin,
                             @Param("idSalidaExcluir") Long idSalidaExcluir);

    /**
     * Indica si el guía tiene otra salida que se cruza con el intervalo dado.
     *
     * @param idGuia          id del guía
     * @param inicio          inicio de la salida
     * @param fin             fin de la salida
     * @param idSalidaExcluir salida a ignorar; {@code null} al crear
     * @return {@code true} si hay cruce
     */
    default boolean guiaTieneCruce(Long idGuia, LocalDateTime inicio, LocalDateTime fin, Long idSalidaExcluir) {
        return contarCrucesDelGuia(idGuia, inicio, fin, idSalidaExcluir) > 0;
    }
}
