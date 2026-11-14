package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.dto.response.ReporteResponse;
import co.edu.uniquindio.QuindioStay_TyA.exception.ReglaNegocioException;
import co.edu.uniquindio.QuindioStay_TyA.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Los 10 reportes de la entrega. Cada uno devuelve sus columnas y filas en un
 * {@link ReporteResponse} para mostrarlo en pantalla o exportarlo a PDF.
 * Los reportes 5 y 10 reciben un rango de fechas (por defecto, los últimos 12 meses).
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    public static final int TOTAL_REPORTES = 10;

    private final ReporteRepository reporteRepository;

    /** Ejecuta el reporte por su número (1 a 10). */
    public ReporteResponse ejecutar(int numero, LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta == null ? LocalDate.now() : hasta;
        LocalDate inicio = desde == null ? fin.minusMonths(12) : desde;
        if (inicio.isAfter(fin)) {
            throw new ReglaNegocioException("La fecha inicial no puede ser posterior a la final");
        }
        return switch (numero) {
            case 1 -> armar(1, "Tours ofertados",
                    "Catálogo de tours con su descripción, edad mínima y precio por persona.",
                    List.of("nombre", "descripcion", "edad_minima", "precio_persona"),
                    reporteRepository.toursOfertados());
            case 2 -> armar(2, "Participación mensual por tour",
                    "Cuántas personas participaron en cada tour, mes a mes (sin reservas canceladas).",
                    List.of("mes", "tour", "participantes"),
                    reporteRepository.participacionMensualPorTour());
            case 3 -> armar(3, "Tours con más cancelaciones",
                    "Tours ordenados por la cantidad de reservas canceladas.",
                    List.of("tour", "reservas_canceladas"),
                    reporteRepository.toursConMasCancelaciones());
            case 4 -> armar(4, "Guías registrados",
                    "Listado de guías con su documento y tarjeta profesional.",
                    List.of("nombre", "documento", "tarjeta_profesional"),
                    reporteRepository.guiasRegistrados());
            case 5 -> armar(5, "Guías con más salidas en un período",
                    "Salidas asignadas por guía entre " + inicio + " y " + fin + " (sin salidas canceladas).",
                    List.of("guia", "salidas_asignadas"),
                    reporteRepository.guiasConMasSalidas(inicio, fin));
            case 6 -> armar(6, "Guías por encima del promedio",
                    "Guías con más salidas asignadas que el promedio general.",
                    List.of("guia", "salidas_asignadas", "promedio_general"),
                    reporteRepository.guiasSobrePromedio());
            case 7 -> armar(7, "Salidas programadas",
                    "Todas las salidas ordenadas por fecha y hora, con su cupo y estado.",
                    List.of("id_salida", "fecha_salida", "hora_salida", "cupo", "estado", "id_tour", "id_guia"),
                    reporteRepository.salidasProgramadas());
            case 8 -> armar(8, "Ocupación por categoría",
                    "Porcentaje promedio de ocupación de las salidas de cada categoría de tour.",
                    List.of("categoria", "salidas", "ocupacion_promedio"),
                    reporteRepository.ocupacionPorCategoria());
            case 9 -> armar(9, "Tours sin participantes",
                    "Tours que no tienen ningún participante en reservas activas.",
                    List.of("tour", "situacion"),
                    reporteRepository.toursSinParticipantes());
            case 10 -> armar(10, "Ingresos por categoría y método de pago",
                    "Dinero recibido (pagos exitosos) entre " + inicio + " y " + fin
                            + ", por mes, categoría y método de pago.",
                    List.of("mes", "categoria", "metodo_pago", "total_recibido"),
                    reporteRepository.ingresosPorCategoriaYMetodo(inicio, fin));
            default -> throw new ReglaNegocioException("Los reportes van del 1 al " + TOTAL_REPORTES);
        };
    }

    /** Lista de los 10 reportes (número, título y descripción) para armar el menú. */
    public List<ReporteResponse> catalogo() {
        return java.util.stream.IntStream.rangeClosed(1, TOTAL_REPORTES)
                .mapToObj(n -> {
                    ReporteResponse r = ejecutar(n, null, null);
                    return new ReporteResponse(r.numero(), r.titulo(), r.descripcion(), r.columnas(), List.of(),
                            r.generadoEn());
                })
                .toList();
    }

    private ReporteResponse armar(int numero, String titulo, String descripcion, List<String> columnas,
                                  List<Map<String, Object>> filas) {
        return new ReporteResponse(numero, titulo, descripcion, columnas, filas, LocalDateTime.now());
    }
}
