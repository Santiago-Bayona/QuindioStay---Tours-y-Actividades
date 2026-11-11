package co.edu.uniquindio.QuindioStay_TyA.repository;

import co.edu.uniquindio.QuindioStay_TyA.domain.Idioma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Acceso a datos de {@link Idioma}. Catálogo: CRUD heredado más búsqueda por nombre.
 */
public interface IdiomaRepository extends JpaRepository<Idioma, Long> {

    /**
     * Busca un idioma por su nombre.
     *
     * @param nombreIdioma nombre del idioma
     * @return el idioma, o vacío si no existe
     */
    @Query("SELECT i FROM Idioma i WHERE i.nombreIdioma = :nombreIdioma")
    Optional<Idioma> buscarPorNombre(@Param("nombreIdioma") String nombreIdioma);
}
