package co.edu.uniquindio.QuindioStay_TyA.repository;

import co.edu.uniquindio.QuindioStay_TyA.domain.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Acceso a datos de {@link Municipio}.
 * <p>
 * Hereda de {@link JpaRepository} el CRUD completo: save, findById, findAll,
 * deleteById, existsById y count.
 */
public interface MunicipioRepository extends JpaRepository<Municipio, Long> {

    /**
     * Busca un municipio por su código DANE.
     *
     * @param codigoDane código DANE de 5 dígitos
     * @return el municipio, o vacío si no existe
     */
    @Query("SELECT m FROM Municipio m WHERE m.codigoDane = :codigoDane")
    Optional<Municipio> buscarPorDane(@Param("codigoDane") String codigoDane);

    /**
     * Busca un municipio por su nombre exacto.
     *
     * @param nombre nombre del municipio
     * @return el municipio, o vacío si no existe
     */
    @Query("SELECT m FROM Municipio m WHERE m.nombre = :nombre")
    Optional<Municipio> buscarPorNombre(@Param("nombre") String nombre);
}
