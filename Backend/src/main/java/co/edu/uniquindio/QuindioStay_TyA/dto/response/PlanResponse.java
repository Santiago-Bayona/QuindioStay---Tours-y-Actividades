package co.edu.uniquindio.QuindioStay_TyA.dto.response;

import co.edu.uniquindio.QuindioStay_TyA.domain.Plan;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.EstadoPlan;
import co.edu.uniquindio.QuindioStay_TyA.domain.enums.TipoPlan;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Plan devuelto por la API.
 */
public record PlanResponse(
        Long idPlan,
        String nombre,
        String descripcion,
        TipoPlan tipo,
        BigDecimal precio,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        EstadoPlan estado,
        Long idPrestador,
        String prestador
) {

    public static PlanResponse de(Plan p) {
        return new PlanResponse(
                p.getIdPlan(), p.getNombre(), p.getDescripcion(), p.getTipo(), p.getPrecio(),
                p.getFechaInicio(), p.getFechaFin(), p.getEstado(),
                p.getPrestador().getIdPrestador(), p.getPrestador().getNombreComercial());
    }
}
