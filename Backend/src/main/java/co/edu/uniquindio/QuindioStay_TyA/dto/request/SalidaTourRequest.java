package co.edu.uniquindio.QuindioStay_TyA.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Datos para programar o editar una salida de un tour.
 */
public record SalidaTourRequest(
        @NotNull(message = "La fecha de salida es obligatoria")
        LocalDate fechaSalida,

        @NotNull(message = "La hora de salida es obligatoria")
        LocalTime horaSalida,

        @NotNull(message = "El cupo es obligatorio")
        @Min(value = 1, message = "El cupo debe ser mayor que cero")
        Integer cupo,

        @NotNull(message = "El tour es obligatorio")
        Long idTour,

        @NotNull(message = "El guía es obligatorio")
        Long idGuia,

        @NotNull(message = "El idioma de la salida es obligatorio")
        Long idIdioma
) {
}
