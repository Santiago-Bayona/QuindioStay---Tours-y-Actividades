package co.edu.uniquindio.QuindioStay_TyA.repository;

import co.edu.uniquindio.QuindioStay_TyA.domain.Guia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de {@link Guia}.
 * <p>
 * Agregar o quitar idiomas no necesita consultas propias: se hace con
 * {@link Guia#agregarIdioma} y {@link Guia#quitarIdioma} y luego {@code save};
 * JPA actualiza la tabla {@code guia_idioma}.
 */
public interface GuiaRepository extends JpaRepository<Guia, Long> {

    /**
     * Lista los guías que hablan un idioma; sirve para ofrecer solo guías
     * válidos al programar una salida en ese idioma.
     *
     * @param idIdioma id del idioma
     * @return guías ordenados por nombre
     */
    @Query("SELECT g FROM Guia g JOIN g.idiomas i WHERE i.idIdioma = :idIdioma ORDER BY g.nombre")
    List<Guia> listarPorIdioma(@Param("idIdioma") Long idIdioma);

    /**
     * Busca un guía por su documento.
     *
     * @param documento documento de identidad
     * @return el guía, o vacío si no existe
     */
    @Query("SELECT g FROM Guia g WHERE g.documento = :documento")
    Optional<Guia> buscarPorDocumento(@Param("documento") String documento);

    /**
     * Carga un guía junto con sus idiomas, para validar el idioma de una salida
     * sin problemas de carga perezosa.
     *
     * @param idGuia id del guía
     * @return el guía con sus idiomas, o vacío si no existe
     */
    @Query("SELECT g FROM Guia g LEFT JOIN FETCH g.idiomas WHERE g.idGuia = :idGuia")
    Optional<Guia> buscarConIdiomas(@Param("idGuia") Long idGuia);
}
