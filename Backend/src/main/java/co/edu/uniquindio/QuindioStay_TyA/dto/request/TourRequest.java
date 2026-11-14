package co.edu.uniquindio.QuindioStay_TyA.dto.request;

import co.edu.uniquindio.QuindioStay_TyA.domain.enums.Dificultad;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Datos para crear o editar un tour.
 */
public record TourRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre admite máximo 120 caracteres")
        String nombre,

        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,

        @NotNull(message = "La duración es obligatoria")
        @DecimalMin(value = "0.00", inclusive = false, message = "La duración debe ser mayor que cero")
        @Digits(integer = 3, fraction = 2, message = "La duración admite máximo 2 decimales")
        BigDecimal duracionHoras,

        @NotNull(message = "La dificultad es obligatoria")
        Dificultad dificultad,

        @NotNull(message = "La edad mínima es obligatoria (use 0 si no hay restricción)")
        @Min(value = 0, message = "La edad mínima no puede ser negativa")
        Integer edadMinima,

        @NotNull(message = "El precio por persona es obligatorio")
        @DecimalMin(value = "0.00", inclusive = false, message = "El precio por persona debe ser mayor que cero")
        @Digits(integer = 10, fraction = 2, message = "El precio admite máximo 2 decimales")
        BigDecimal precioPersona,

        @NotNull(message = "La categoría es obligatoria")
        Long idCategoria,

        @NotNull(message = "El prestador es obligatorio")
        Long idPrestador
) {
}
