package co.edu.uniquindio.QuindioStay_TyA.repository;

import co.edu.uniquindio.QuindioStay_TyA.domain.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Acceso a datos de {@link Tour}.
 */
public interface TourRepository extends JpaRepository<Tour, Long> {

    /**
     * Lista los tours de una categoría (filtro del catálogo para el viajero).
     *
     * @param idCategoria id de la categoría
     * @return tours ordenados por nombre
     */
    @Query("SELECT t FROM Tour t WHERE t.categoria.idCategoria = :idCategoria ORDER BY t.nombre")
    List<Tour> listarPorCategoria(@Param("idCategoria") Long idCategoria);

    /**
     * Lista los tours que publicó un prestador.
     *
     * @param idPrestador id del prestador
     * @return tours ordenados por nombre
     */
    @Query("SELECT t FROM Tour t WHERE t.prestador.idPrestador = :idPrestador ORDER BY t.nombre")
    List<Tour> listarPorPrestador(@Param("idPrestador") Long idPrestador);

    /**
     * Lista los tours publicados por prestadores activos: es el catálogo visible.
     *
     * @return tours ordenados por nombre
     */
    @Query("""
            SELECT t FROM Tour t
            WHERE t.prestador.estado = co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPrestador.ACTIVO
            ORDER BY t.nombre
            """)
    List<Tour> listarDisponibles();
}
