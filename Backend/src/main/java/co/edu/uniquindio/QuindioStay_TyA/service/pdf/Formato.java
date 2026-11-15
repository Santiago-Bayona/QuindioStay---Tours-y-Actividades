package co.edu.uniquindio.QuindioStay_TyA.service.pdf;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Utilidades para mostrar datos en los PDF con formato colombiano:
 * dinero ({@code $ 240.000}), fechas ({@code 17/01/2026}) y valores de enums
 * legibles ({@code TARJETA_CREDITO} → {@code Tarjeta de crédito}).
 */
public final class Formato {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    /** Textos con tilde para los valores de enum que lo necesitan; los demás se arman solos. */
    private static final Map<String, String> TEXTOS = Map.ofEntries(
            Map.entry("TARJETA_CREDITO", "Tarjeta de crédito"),
            Map.entry("TARJETA_DEBITO", "Tarjeta débito"),
            Map.entry("CEDULA_CIUDADANIA", "Cédula de ciudadanía"),
            Map.entry("CEDULA_EXTRANJERIA", "Cédula de extranjería"),
            Map.entry("APP_MOVIL", "App móvil"),
            Map.entry("TELEFONO", "Teléfono"),
            Map.entry("BASICO", "Básico"),
            Map.entry("ESTANDAR", "Estándar"),
            Map.entry("ESPANOL", "Español"),
            Map.entry("INGLES", "Inglés"),
            Map.entry("PSE", "PSE"),
            Map.entry("SMS", "SMS")
    );

    private Formato() {
    }

    /** @return el valor en pesos sin decimales innecesarios, p. ej. {@code $ 240.000} */
    public static String dinero(BigDecimal valor) {
        if (valor == null) {
            return "$ 0";
        }
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.of("es", "CO"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        return "$ " + new DecimalFormat("#,##0.##", simbolos).format(valor);
    }

    /** @return número con separador de miles, p. ej. {@code 1.250} o {@code 22,5} */
    public static String numero(Number valor) {
        if (valor == null) {
            return "";
        }
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.of("es", "CO"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        return new DecimalFormat("#,##0.##", simbolos).format(valor);
    }

    /** @return fecha como {@code dd/MM/yyyy}; vacío si es {@code null} */
    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FECHA);
    }

    /** @return fecha y hora como {@code dd/MM/yyyy HH:mm}; vacío si es {@code null} */
    public static String fechaHora(LocalDateTime fechaHora) {
        return fechaHora == null ? "" : fechaHora.format(FECHA_HORA);
    }

    /** @return hora como {@code HH:mm}; vacío si es {@code null} */
    public static String hora(LocalTime hora) {
        return hora == null ? "" : hora.format(HORA);
    }

    /**
     * Convierte el nombre de un enum en texto legible.
     *
     * @param valor por ejemplo {@code EstadoReserva.CONFIRMADA}
     * @return por ejemplo {@code Confirmada}
     */
    public static String texto(Enum<?> valor) {
        return valor == null ? "" : texto(valor.name());
    }

    /**
     * Convierte un nombre en mayúsculas con guiones bajos en texto legible.
     *
     * @param valor por ejemplo {@code AGENCIA_INTERMEDIARIA}
     * @return por ejemplo {@code Agencia intermediaria}
     */
    public static String texto(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        String conocido = TEXTOS.get(valor);
        if (conocido != null) {
            return conocido;
        }
        String minusculas = valor.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(minusculas.charAt(0)) + minusculas.substring(1);
    }

    /**
     * Arma el número visible de un documento.
     *
     * @param prefijo por ejemplo {@code "R"} para reservas
     * @param id      id del registro
     * @return por ejemplo {@code R-000012}
     */
    public static String consecutivo(String prefijo, Long id) {
        return prefijo + "-" + String.format("%06d", id);
    }
}
