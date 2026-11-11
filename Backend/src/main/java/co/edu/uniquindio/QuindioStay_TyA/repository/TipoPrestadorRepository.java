package co.edu.uniquindio.QuindioStay_TyA.repository;

import co.edu.uniquindio.QuindioStay_TyA.domain.TipoPrestador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Acceso a datos de {@link TipoPrestador}. Catálogo: con el CRUD heredado basta,
 * más una búsqueda por nombre para validar que no se repita.
 */
public interface TipoPrestadorRepository extends JpaRepository<TipoPrestador, Long> {

    /**
     * Busca un tipo de prestador por su nombre.
     *
     * @param nombreTipo nombre del tipo
     * @return el tipo, o vacío si no existe
     */
    @Query("SELECT t FROM TipoPrestador t WHERE t.nombreTipo = :nombreTipo")
    Optional<TipoPrestador> buscarPorNombre(@Param("nombreTipo") String nombreTipo);
}
