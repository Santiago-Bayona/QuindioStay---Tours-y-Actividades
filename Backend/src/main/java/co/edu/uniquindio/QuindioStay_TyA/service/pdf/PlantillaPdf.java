package co.edu.uniquindio.QuindioStay_TyA.service.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Plantilla base de todos los PDF del sistema (reportes, comprobantes, factura y
 * autorizaciones), para que compartan el mismo encabezado, colores y pie de página.
 * <p>
 * Uso:
 * <pre>{@code
 * PlantillaPdf pdf = PlantillaPdf.vertical("Comprobante de reserva", "N.º R-000012");
 * pdf.seccion("Datos del cliente");
 * pdf.datos(Map.of("Nombre", "Valentina Rojas", "Documento", "1094123456"));
 * pdf.tabla(new String[]{"#", "Nombre"}, new float[]{1, 6}, filas, 0);
 * byte[] contenido = pdf.terminar();
 * }</pre>
 * Cada instancia genera un solo documento; no es un bean de Spring.
 */
public class PlantillaPdf {

    /** Verde café del Quindío: color principal de la marca. */
    public static final Color PRIMARIO = new Color(0x1F, 0x4D, 0x3A);
    /** Color de acento (dorado café). */
    public static final Color ACENTO = new Color(0xB8, 0x86, 0x2B);
    private static final Color TEXTO = new Color(0x22, 0x2B, 0x27);
    private static final Color TEXTO_SUAVE = new Color(0x5F, 0x6B, 0x66);
    private static final Color FONDO_SUAVE = new Color(0xF2, 0xF5, 0xF3);
    private static final Color BORDE = new Color(0xD5, 0xDD, 0xD9);

    private static final Font MARCA = new Font(Font.HELVETICA, 17, Font.BOLD, PRIMARIO);
    private static final Font MARCA_SUB = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, TEXTO_SUAVE);
    private static final Font TITULO = new Font(Font.HELVETICA, 13, Font.BOLD, TEXTO);
    private static final Font SUBTITULO = new Font(Font.HELVETICA, 9, Font.NORMAL, TEXTO_SUAVE);
    private static final Font SECCION = new Font(Font.HELVETICA, 10.5f, Font.BOLD, PRIMARIO);
    private static final Font ETIQUETA = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, TEXTO_SUAVE);
    private static final Font VALOR = new Font(Font.HELVETICA, 9.5f, Font.NORMAL, TEXTO);
    private static final Font NORMAL = new Font(Font.HELVETICA, 9.5f, Font.NORMAL, TEXTO);
    private static final Font NEGRITA = new Font(Font.HELVETICA, 9.5f, Font.BOLD, TEXTO);
    private static final Font ENCABEZADO_TABLA = new Font(Font.HELVETICA, 8.5f, Font.BOLD, Color.WHITE);
    private static final Font CELDA = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, TEXTO);
    private static final Font PEQUENA = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, TEXTO_SUAVE);
    private static final Font TOTAL = new Font(Font.HELVETICA, 11, Font.BOLD, PRIMARIO);

    private final Document documento;
    private final ByteArrayOutputStream salida = new ByteArrayOutputStream();

    private PlantillaPdf(Rectangle tamano, String titulo, String subtitulo) {
        documento = new Document(tamano, 42, 42, 40, 54);
        PdfWriter writer = PdfWriter.getInstance(documento, salida);
        writer.setPageEvent(new PiePagina());
        documento.addTitle(titulo);
        documento.addCreator("QuindíoStay - Tours y Actividades");
        documento.open();
        encabezado(titulo, subtitulo);
    }

    /** Crea un documento tamaño carta vertical (comprobantes, factura, autorizaciones). */
    public static PlantillaPdf vertical(String titulo, String subtitulo) {
        return new PlantillaPdf(PageSize.LETTER, titulo, subtitulo);
    }

    /** Crea un documento tamaño carta horizontal (reportes con muchas columnas). */
    public static PlantillaPdf horizontal(String titulo, String subtitulo) {
        return new PlantillaPdf(PageSize.LETTER.rotate(), titulo, subtitulo);
    }

    // ------------------------------------------------------------------ bloques de contenido

    /** Agrega un título de sección con una línea de color debajo. */
    public void seccion(String texto) {
        Paragraph p = new Paragraph(texto, SECCION);
        p.setSpacingBefore(12);
        p.setSpacingAfter(2);
        agregar(p);
        linea(PRIMARIO, 0.8f, 6);
    }

    /**
     * Agrega pares etiqueta/valor en una cuadrícula de dos columnas, en el orden del mapa
     * (use {@link java.util.LinkedHashMap}).
     */
    public void datos(Map<String, String> pares) {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        for (Map.Entry<String, String> par : pares.entrySet()) {
            PdfPCell celda = new PdfPCell();
            celda.setBorder(Rectangle.NO_BORDER);
            celda.setPaddingBottom(5);
            celda.addElement(new Paragraph(par.getKey().toUpperCase(), ETIQUETA));
            Paragraph valor = new Paragraph(vacio(par.getValue()), VALOR);
            valor.setLeading(12);
            celda.addElement(valor);
            tabla.addCell(celda);
        }
        if (pares.size() % 2 == 1) {
            PdfPCell relleno = new PdfPCell();
            relleno.setBorder(Rectangle.NO_BORDER);
            tabla.addCell(relleno);
        }
        agregar(tabla);
    }

    /**
     * Agrega una tabla con encabezado de color y filas alternadas. El encabezado se repite
     * si la tabla pasa a otra página.
     *
     * @param encabezados      títulos de las columnas
     * @param anchos           ancho relativo de cada columna
     * @param filas            valores ya formateados
     * @param columnasDerecha  índices de columnas numéricas que se alinean a la derecha
     */
    public void tabla(String[] encabezados, float[] anchos, List<String[]> filas, int... columnasDerecha) {
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        tabla.setSpacingBefore(2);
        for (int i = 0; i < encabezados.length; i++) {
            PdfPCell celda = new PdfPCell(new Phrase(encabezados[i], ENCABEZADO_TABLA));
            celda.setBackgroundColor(PRIMARIO);
            celda.setBorderColor(PRIMARIO);
            celda.setPadding(5);
            celda.setHorizontalAlignment(esDerecha(i, columnasDerecha) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
            tabla.addCell(celda);
        }
        if (filas.isEmpty()) {
            PdfPCell celda = new PdfPCell(new Phrase("No hay datos para mostrar.", PEQUENA));
            celda.setColspan(encabezados.length);
            celda.setPadding(8);
            celda.setBorderColor(BORDE);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(celda);
        }
        for (int f = 0; f < filas.size(); f++) {
            String[] fila = filas.get(f);
            for (int i = 0; i < encabezados.length; i++) {
                String valor = i < fila.length ? fila[i] : "";
                PdfPCell celda = new PdfPCell(new Phrase(vacio(valor), CELDA));
                celda.setPadding(4.5f);
                celda.setBorderColor(BORDE);
                celda.setBorderWidth(0.5f);
                if (f % 2 == 1) {
                    celda.setBackgroundColor(FONDO_SUAVE);
                }
                celda.setHorizontalAlignment(esDerecha(i, columnasDerecha) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
                tabla.addCell(celda);
            }
        }
        agregar(tabla);
    }

    /**
     * Agrega un recuadro de totales alineado a la derecha. La última fila se resalta
     * (p. ej. "Saldo pendiente" o "Total").
     */
    public void resumen(Map<String, String> filas) {
        PdfPTable tabla = new PdfPTable(new float[]{3, 2});
        tabla.setWidthPercentage(48);
        tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.setSpacingBefore(8);
        int i = 0;
        for (Map.Entry<String, String> fila : filas.entrySet()) {
            boolean ultima = ++i == filas.size();
            PdfPCell etiqueta = new PdfPCell(new Phrase(fila.getKey(), ultima ? NEGRITA : NORMAL));
            PdfPCell valor = new PdfPCell(new Phrase(fila.getValue(), ultima ? TOTAL : NORMAL));
            for (PdfPCell c : new PdfPCell[]{etiqueta, valor}) {
                c.setBorder(ultima ? Rectangle.TOP : Rectangle.NO_BORDER);
                c.setBorderColor(PRIMARIO);
                c.setBorderWidth(1f);
                c.setPadding(4);
                if (ultima) {
                    c.setPaddingTop(6);
                    c.setBackgroundColor(FONDO_SUAVE);
                }
            }
            valor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tabla.addCell(etiqueta);
            tabla.addCell(valor);
        }
        agregar(tabla);
    }

    /** Agrega un párrafo justificado. */
    public void parrafo(String texto) {
        Paragraph p = new Paragraph(texto, NORMAL);
        p.setAlignment(Element.ALIGN_JUSTIFIED);
        p.setLeading(14);
        p.setSpacingAfter(6);
        agregar(p);
    }

    /** Agrega una lista numerada de declaraciones o condiciones. */
    public void lista(List<String> items) {
        int n = 1;
        for (String item : items) {
            Paragraph p = new Paragraph();
            p.add(new Chunk(n++ + ".  ", NEGRITA));
            p.add(new Chunk(item, NORMAL));
            p.setAlignment(Element.ALIGN_JUSTIFIED);
            p.setIndentationLeft(14);
            p.setFirstLineIndent(-14);
            p.setLeading(13.5f);
            p.setSpacingAfter(4);
            agregar(p);
        }
    }

    /**
     * Agrega espacios para firma uno al lado del otro. Cada elemento es el texto bajo la
     * línea; puede tener varias líneas separadas por {@code \n}.
     */
    public void firmas(String... textos) {
        PdfPTable tabla = new PdfPTable(textos.length);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(46);
        tabla.setKeepTogether(true);
        for (String texto : textos) {
            PdfPCell celda = new PdfPCell();
            celda.setBorder(Rectangle.NO_BORDER);
            celda.setPaddingLeft(12);
            celda.setPaddingRight(12);
            PdfPTable linea = new PdfPTable(1);
            linea.setWidthPercentage(100);
            PdfPCell l = new PdfPCell(new Phrase(texto, PEQUENA));
            l.setBorder(Rectangle.TOP);
            l.setBorderColor(TEXTO);
            l.setPaddingTop(4);
            linea.addCell(l);
            celda.addElement(linea);
            tabla.addCell(celda);
        }
        agregar(tabla);
    }

    /** Agrega un recuadro de nota o advertencia con borde de acento a la izquierda. */
    public void nota(String texto) {
        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(10);
        PdfPCell celda = new PdfPCell(new Phrase(texto, PEQUENA));
        celda.setBorder(Rectangle.LEFT);
        celda.setBorderColorLeft(ACENTO);
        celda.setBorderWidthLeft(3);
        celda.setBackgroundColor(FONDO_SUAVE);
        celda.setPadding(7);
        celda.setLeading(0, 1.3f);
        tabla.addCell(celda);
        agregar(tabla);
    }

    /** Agrega texto pequeño de apoyo, p. ej. "12 registros". */
    public void textoPequeno(String texto) {
        Paragraph p = new Paragraph(texto, PEQUENA);
        p.setSpacingBefore(4);
        agregar(p);
    }

    /** Cierra el documento y devuelve sus bytes. */
    public byte[] terminar() {
        documento.close();
        return salida.toByteArray();
    }

    // ------------------------------------------------------------------ internos

    private void encabezado(String titulo, String subtitulo) {
        PdfPTable tabla = new PdfPTable(new float[]{5, 5});
        tabla.setWidthPercentage(100);

        PdfPCell marca = new PdfPCell();
        marca.setBorder(Rectangle.NO_BORDER);
        Paragraph nombre = new Paragraph();
        nombre.add(new Chunk("Quindío", MARCA));
        nombre.add(new Chunk("Stay", new Font(Font.HELVETICA, 17, Font.BOLD, ACENTO)));
        marca.addElement(nombre);
        marca.addElement(new Paragraph("Tours y Actividades · Quindío, Colombia", MARCA_SUB));

        PdfPCell doc = new PdfPCell();
        doc.setBorder(Rectangle.NO_BORDER);
        Paragraph t = new Paragraph(titulo, TITULO);
        t.setAlignment(Element.ALIGN_RIGHT);
        doc.addElement(t);
        if (subtitulo != null && !subtitulo.isBlank()) {
            Paragraph s = new Paragraph(subtitulo, SUBTITULO);
            s.setAlignment(Element.ALIGN_RIGHT);
            doc.addElement(s);
        }

        tabla.addCell(marca);
        tabla.addCell(doc);
        agregar(tabla);
        linea(ACENTO, 2f, 8);
    }

    private void linea(Color color, float grosor, float espacioDespues) {
        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(espacioDespues);
        PdfPCell celda = new PdfPCell();
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(color);
        celda.setBorderWidth(grosor);
        celda.setFixedHeight(2);
        tabla.addCell(celda);
        agregar(tabla);
    }

    private void agregar(Element elemento) {
        documento.add(elemento);
    }

    private static boolean esDerecha(int columna, int[] columnasDerecha) {
        for (int c : columnasDerecha) {
            if (c == columna) {
                return true;
            }
        }
        return false;
    }

    private static String vacio(String valor) {
        return valor == null || valor.isBlank() ? "—" : valor;
    }

    /** Pie de página: fecha de generación a la izquierda y "Página X de Y" a la derecha. */
    private static class PiePagina extends PdfPageEventHelper {

        private final String generado = "QuindíoStay · Generado el " + Formato.fechaHora(LocalDateTime.now());
        private PdfTemplate totalPaginas;
        private BaseFont fuente;

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalPaginas = writer.getDirectContent().createTemplate(30, 12);
            try {
                fuente = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                throw new IllegalStateException("No se pudo cargar la fuente del PDF", e);
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            float y = document.bottom() - 22;
            Font pie = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, TEXTO_SUAVE);

            cb.setColorStroke(BORDE);
            cb.setLineWidth(0.5f);
            cb.moveTo(document.left(), y + 11);
            cb.lineTo(document.right(), y + 11);
            cb.stroke();

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(generado, pie), document.left(), y, 0);
            String pagina = "Página " + writer.getPageNumber() + " de ";
            float ancho = fuente.getWidthPoint(pagina, 7.5f);
            float x = document.right() - 14 - ancho;
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(pagina, pie), x, y, 0);
            cb.addTemplate(totalPaginas, x + ancho, y);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            totalPaginas.beginText();
            totalPaginas.setFontAndSize(fuente, 7.5f);
            totalPaginas.setColorFill(TEXTO_SUAVE);
            totalPaginas.showText(String.valueOf(writer.getPageNumber() - 1));
            totalPaginas.endText();
        }
    }
}
