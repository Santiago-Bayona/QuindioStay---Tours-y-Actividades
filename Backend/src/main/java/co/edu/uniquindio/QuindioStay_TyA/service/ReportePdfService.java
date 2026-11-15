package co.edu.uniquindio.QuindioStay_TyA.service;

import co.edu.uniquindio.QuindioStay_TyA.dto.response.ReporteResponse;
import co.edu.uniquindio.QuindioStay_TyA.service.pdf.Formato;
import co.edu.uniquindio.QuindioStay_TyA.service.pdf.PlantillaPdf;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Exporta a PDF los 10 reportes del proyecto.
 * <p>
 * Reutiliza {@link ReporteService#ejecutar}: el PDF muestra exactamente los mismos
 * datos que la tabla de la aplicación, con encabezado, descripción, tabla con
 * formato (dinero, fechas, porcentajes) y número de registros.
 */
@Service
@RequiredArgsConstructor
public class ReportePdfService {

    /** Títulos legibles de las columnas que devuelven las consultas SQL. */
    private static final Map<String, String> COLUMNAS = Map.ofEntries(
            Map.entry("nombre", "Nombre"),
            Map.entry("descripcion", "Descripción"),
            Map.entry("edad_minima", "Edad mínima"),
            Map.entry("precio_persona", "Precio por persona"),
            Map.entry("mes", "Mes"),
            Map.entry("tour", "Tour"),
            Map.entry("participantes", "Participantes"),
            Map.entry("reservas_canceladas", "Reservas canceladas"),
            Map.entry("documento", "Documento"),
            Map.entry("tarjeta_profesional", "Tarjeta profesional"),
            Map.entry("guia", "Guía"),
            Map.entry("salidas_asignadas", "Salidas asignadas"),
            Map.entry("promedio_general", "Promedio general"),
            Map.entry("id_salida", "N.º salida"),
            Map.entry("fecha_salida", "Fecha"),
            Map.entry("hora_salida", "Hora"),
            Map.entry("cupo", "Cupo"),
            Map.entry("estado", "Estado"),
            Map.entry("id_tour", "N.º tour"),
            Map.entry("id_guia", "N.º guía"),
            Map.entry("categoria", "Categoría"),
            Map.entry("salidas", "Salidas"),
            Map.entry("ocupacion_promedio", "Ocupación promedio"),
            Map.entry("situacion", "Situación"),
            Map.entry("metodo_pago", "Método de pago"),
            Map.entry("total_recibido", "Total recibido")
    );

    /** Columnas que son dinero. */
    private static final Set<String> DINERO = Set.of("precio_persona", "total_recibido");
    /** Columnas que son porcentaje. */
    private static final Set<String> PORCENTAJE = Set.of("ocupacion_promedio");
    /** Columnas de texto largo, que reciben más ancho en la tabla. */
    private static final Set<String> TEXTO_LARGO = Set.of("descripcion");
    /** Columnas de nombres, que reciben ancho medio. */
    private static final Set<String> TEXTO_MEDIO = Set.of("nombre", "tour", "guia", "categoria", "situacion",
            "metodo_pago", "tarjeta_profesional");
    /** Columnas con valores de enum guardados en mayúsculas (p. ej. TARJETA_CREDITO). */
    private static final Set<String> ENUMS = Set.of("estado", "metodo_pago");

    private final ReporteService reporteService;

    /**
     * Genera el PDF de un reporte.
     *
     * @param numero número del reporte (1 a 10)
     * @param desde  inicio del periodo (opcional; lo usan los reportes 5 y 10)
     * @param hasta  fin del periodo (opcional)
     * @return el PDF en bytes
     */
    public byte[] exportar(int numero, LocalDate desde, LocalDate hasta) {
        ReporteResponse reporte = reporteService.ejecutar(numero, desde, hasta);
        List<String> columnas = reporte.columnas();

        PlantillaPdf pdf = columnas.size() > 5 || columnas.stream().anyMatch(TEXTO_LARGO::contains)
                ? PlantillaPdf.horizontal("Reporte " + numero, reporte.titulo())
                : PlantillaPdf.vertical("Reporte " + numero, reporte.titulo());

        pdf.seccion(reporte.titulo());
        pdf.parrafo(reporte.descripcion());

        String[] encabezados = new String[columnas.size()];
        float[] anchos = new float[columnas.size()];
        List<Integer> derecha = new ArrayList<>();
        for (int i = 0; i < columnas.size(); i++) {
            String columna = columnas.get(i);
            encabezados[i] = COLUMNAS.getOrDefault(columna, Formato.texto(columna.toUpperCase()));
            anchos[i] = TEXTO_LARGO.contains(columna) ? 4.5f : TEXTO_MEDIO.contains(columna) ? 2.4f : 1.3f;
        }

        List<String[]> filas = new ArrayList<>();
        for (Map<String, Object> fila : reporte.filas()) {
            String[] valores = new String[columnas.size()];
            for (int i = 0; i < columnas.size(); i++) {
                String columna = columnas.get(i);
                Object valor = fila.get(columna);
                valores[i] = formatear(columna, valor);
                if (i > 0 && (valor instanceof Number) && !derecha.contains(i)) {
                    derecha.add(i);
                }
            }
            filas.add(valores);
        }

        pdf.tabla(encabezados, anchos, filas, derecha.stream().mapToInt(Integer::intValue).toArray());
        pdf.textoPequeno(filas.size() == 1 ? "1 registro" : filas.size() + " registros");
        return pdf.terminar();
    }

    /** Nombre sugerido del archivo, p. ej. {@code reporte-08.pdf}. */
    public static String nombreArchivo(int numero) {
        return String.format("reporte-%02d.pdf", numero);
    }

    /** Da formato a un valor según su columna y su tipo. */
    private String formatear(String columna, Object valor) {
        if (valor == null) {
            return "";
        }
        if (DINERO.contains(columna) && valor instanceof Number n) {
            return Formato.dinero(new BigDecimal(n.toString()));
        }
        if (PORCENTAJE.contains(columna) && valor instanceof Number n) {
            return Formato.numero(n) + " %";
        }
        if (ENUMS.contains(columna)) {
            return Formato.texto(valor.toString());
        }
        if (valor instanceof Number n) {
            return Formato.numero(n);
        }
        if (valor instanceof java.sql.Date d) {
            return Formato.fecha(d.toLocalDate());
        }
        if (valor instanceof java.sql.Time t) {
            return Formato.hora(t.toLocalTime());
        }
        if (valor instanceof java.sql.Timestamp ts) {
            return Formato.fechaHora(ts.toLocalDateTime());
        }
        if (valor instanceof LocalDate d) {
            return Formato.fecha(d);
        }
        if (valor instanceof LocalTime t) {
            return Formato.hora(t);
        }
        if (valor instanceof LocalDateTime dt) {
            return Formato.fechaHora(dt);
        }
        return valor.toString();
    }
}
