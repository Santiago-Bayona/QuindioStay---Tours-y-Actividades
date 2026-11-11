package co.edu.uniquindio.QuindioStay_TyA.repository;

import co.edu.uniquindio.QuindioStay_TyA.domain.CategoriaTour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Acceso a datos de {@link CategoriaTour}. Catálogo: CRUD heredado más búsqueda por nombre.
 */
public interface CategoriaTourRepository extends JpaRepository<CategoriaTour, Long> {

    /**
     * Busca una categoría por su nombre.
     *
     * @param nombre nombre de la categoría
     * @return la categoría, o vacío si no existe
     */
    @Query("SELECT c FROM CategoriaTour c WHERE c.nombre = :nombre")
    Optional<CategoriaTour> buscarPorNombre(@Param("nombre") String nombre);
}
