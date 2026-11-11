package co.edu.uniquindio.QuindioStay_TyA.repository;

import co.edu.uniquindio.QuindioStay_TyA.domain.Prestador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de {@link Prestador}.
 * <p>
 * No se usa {@code deleteById}: un prestador nunca se borra, se retira con
 * {@link Prestador#retirar()}.
 */
public interface PrestadorRepository extends JpaRepository<Prestador, Long> {

    /**
     * Lista los prestadores de un municipio.
     *
     * @param idMunicipio id del municipio
     * @return prestadores ordenados por nombre comercial
     */
    @Query("SELECT p FROM Prestador p WHERE p.municipio.idMunicipio = :idMunicipio ORDER BY p.nombreComercial")
    List<Prestador> listarPorMunicipio(@Param("idMunicipio") Long idMunicipio);

    /**
     * Lista los prestadores de un tipo.
     *
     * @param idTipoPrestador id del tipo de prestador
     * @return prestadores ordenados por nombre comercial
     */
    @Query("SELECT p FROM Prestador p WHERE p.tipoPrestador.idTipoPrestador = :idTipo ORDER BY p.nombreComercial")
    List<Prestador> listarPorTipo(@Param("idTipo") Long idTipoPrestador);

    /**
     * Busca un prestador por su NIT o documento.
     *
     * @param nitDocumento NIT o documento
     * @return el prestador, o vacío si no existe
     */
    @Query("SELECT p FROM Prestador p WHERE p.nitDocumento = :nit")
    Optional<Prestador> buscarPorNit(@Param("nit") String nitDocumento);
}
