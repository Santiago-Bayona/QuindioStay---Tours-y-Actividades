package co.edu.uniquindio.QuindioStay_TyA.controller;

import co.edu.uniquindio.QuindioStay_TyA.dto.response.ReporteResponse;
import co.edu.uniquindio.QuindioStay_TyA.service.AutorizacionService;
import co.edu.uniquindio.QuindioStay_TyA.service.ReportePdfService;
import co.edu.uniquindio.QuindioStay_TyA.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Los 10 reportes del proyecto (solo usuarios internos).
 * <p>
 * Cada reporte devuelve título, descripción, columnas y filas, listo para pintar
 * en una tabla en React, y se puede exportar a PDF con {@code /{numero}/pdf}.
 */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;
    private final ReportePdfService reportePdfService;
    private final AutorizacionService autorizacion;

    /** {@code GET /api/reportes}: número, título, descripción y columnas de los 10 reportes (para el menú). */
    @GetMapping
    public List<ReporteResponse> catalogo(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario) {
        autorizacion.exigirUsuarioInterno(idUsuario);
        return reporteService.catalogo();
    }

    /**
     * {@code GET /api/reportes/{numero}?desde=2026-01-01&hasta=2026-12-31}: ejecuta un reporte.
     * El periodo es opcional (por defecto, los últimos 12 meses) y solo lo usan los
     * reportes 5 y 10. Un número fuera de 1 a 10 responde 400.
     */
    @GetMapping("/{numero}")
    public ReporteResponse ejecutar(@RequestHeader(Sesion.HEADER_USUARIO) Long idUsuario,
                                    @PathVariable int numero,
                                    @RequestParam(required = false)
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                    @RequestParam(required = false)
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        autorizacion.exigirUsuarioInterno(idUsuario);
        return reporteService.ejecutar(numero, desde, hasta);
    }

    /**
     * {@code GET /api/reportes/{numero}/pdf?desde=&hasta=&descargar=true}: exporta el reporte a PDF.
     * <p>
     * Como el navegador no envía headers al abrir un enlace, el usuario de la sesión también se
     * acepta como parámetro {@code ?idUsuario=}. Ejemplo desde React:
     * {@code window.open(`/api/reportes/8/pdf?idUsuario=${sesion.id}`)}.
     */
    @GetMapping("/{numero}/pdf")
    public ResponseEntity<byte[]> exportarPdf(@RequestHeader(value = Sesion.HEADER_USUARIO, required = false) Long idHeader,
                                              @RequestParam(required = false) Long idUsuario,
                                              @PathVariable int numero,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                              @RequestParam(defaultValue = "false") boolean descargar) {
        autorizacion.exigirUsuarioInterno(idHeader != null ? idHeader : idUsuario);
        return RespuestaPdf.de(reportePdfService.exportar(numero, desde, hasta),
                ReportePdfService.nombreArchivo(numero), descargar);
    }
}
