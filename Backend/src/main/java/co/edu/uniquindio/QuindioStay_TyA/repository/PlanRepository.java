package co.edu.uniquindio.QuindioStay_TyA.repository;

import co.edu.uniquindio.QuindioStay_TyA.domain.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Acceso a datos de {@link Plan}.
 * <p>
 * Antes de borrar un plan, el servicio consulta
 * {@link ReservaRepository#existePorPlan(Long)}: si ya se vendió, se inactiva.
 */
public interface PlanRepository extends JpaRepository<Plan, Long> {

    /**
     * Lista los planes de un prestador.
     *
     * @param idPrestador id del prestador
     * @return planes ordenados por nombre
     */
    @Query("SELECT p FROM Plan p WHERE p.prestador.idPrestador = :idPrestador ORDER BY p.nombre")
    List<Plan> listarPorPrestador(@Param("idPrestador") Long idPrestador);

    /**
     * Lista los planes activos cuya vigencia incluye una fecha.
     *
     * @param fecha fecha de referencia, normalmente la del servicio
     * @return planes vigentes en esa fecha
     */
    @Query("""
            SELECT p FROM Plan p
            WHERE p.estado = co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPlan.ACTIVO
              AND :fecha BETWEEN p.fechaInicio AND p.fechaFin
            ORDER BY p.nombre
            """)
    List<Plan> listarVigentes(@Param("fecha") LocalDate fecha);

    /**
     * Lista los planes vigentes de un prestador; son los que el cliente puede
     * elegir al reservar un tour de ese prestador.
     *
     * @param idPrestador id del prestador del tour
     * @param fecha       fecha del servicio
     * @return planes del prestador vigentes en esa fecha
     */
    @Query("""
            SELECT p FROM Plan p
            WHERE p.prestador.idPrestador = :idPrestador
              AND p.estado = co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPlan.ACTIVO
              AND :fecha BETWEEN p.fechaInicio AND p.fechaFin
            ORDER BY p.precio
            """)
    List<Plan> listarVigentesPorPrestador(@Param("idPrestador") Long idPrestador,
                                          @Param("fecha") LocalDate fecha);
}
