package sv.udb.cafedonbosco.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import sv.udb.cafedonbosco.dto.response.DetalleVentaResponseDTO;
import sv.udb.cafedonbosco.dto.response.OpcionSeleccionadaResponseDTO;
import sv.udb.cafedonbosco.dto.response.VentaResponseDTO;
import sv.udb.cafedonbosco.exception.ErrorInternoException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Genera el PDF real del ticket de una venta con PDFBox. Es una
 * representacion alternativa de los mismos datos que ya muestra
 * ticket.jsp (no lo reemplaza ni cambia su diseno visual): sirve para
 * que el cliente pueda descargar o imprimir un comprobante formal.
 */
public final class TicketPdfGenerator {

    private static final float MARGEN_IZQUIERDO = 50f;
    private static final float ANCHO_PAGINA = PDRectangle.A4.getWidth() - 2 * MARGEN_IZQUIERDO;
    private static final PDFont FUENTE = PDType1Font.HELVETICA;
    private static final PDFont FUENTE_NEGRITA = PDType1Font.HELVETICA_BOLD;

    private TicketPdfGenerator() {
    }

    public static byte[] generar(VentaResponseDTO venta) {
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);

            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
                float y = PDRectangle.A4.getHeight() - 60;

                y = escribirTitulo(contenido, y);
                y = escribirLinea(contenido, FUENTE, 10, y - 10, "Ticket de venta #" + venta.getId());
                y = escribirLinea(contenido, FUENTE, 10, y, "Tipo: " + venta.getTipoVenta());
                y = escribirLinea(contenido, FUENTE, 10, y, "Fecha: " + venta.getFechaFormateada() + " " + venta.getHoraFormateada());
                y = escribirLinea(contenido, FUENTE, 10, y, "Estado del pedido: " + venta.getEstado());
                y = escribirLinea(contenido, FUENTE, 10, y, "Estado del pago: " + venta.getEstadoPago());
                y = escribirLinea(contenido, FUENTE, 10, y, "Metodo de pago: " + venta.getMetodoPago());

                if (venta.getNombreCliente() != null) {
                    y = escribirLinea(contenido, FUENTE, 10, y - 6, "Cliente: " + venta.getNombreCliente());
                    if (venta.getTelefonoCliente() != null) {
                        y = escribirLinea(contenido, FUENTE, 10, y, "Telefono: " + venta.getTelefonoCliente());
                    }
                    if (venta.getTipoEntrega() != null) {
                        y = escribirLinea(contenido, FUENTE, 10, y, "Entrega: " + venta.getTipoEntrega()
                                + (venta.getDireccionCliente() != null ? " - " + venta.getDireccionCliente() : ""));
                    }
                }

                y -= 15;
                y = escribirLinea(contenido, FUENTE_NEGRITA, 11, y, "Detalle");
                y -= 4;
                y = escribirFilaDetalle(contenido, y, "Producto", "Cant.", "P. Unit.", "Subtotal", true);
                y -= 2;
                y = trazarLinea(contenido, y);

                if (venta.getDetalles() != null) {
                    for (DetalleVentaResponseDTO detalle : venta.getDetalles()) {
                        y = escribirFilaDetalle(contenido, y,
                                detalle.getNombreProducto(),
                                String.valueOf(detalle.getCantidad()),
                                FormatoUtil.moneda(detalle.getPrecioUnitario()),
                                FormatoUtil.moneda(detalle.getSubtotal()),
                                false);
                        if (!detalle.getOpciones().isEmpty()) {
                            y = escribirOpciones(contenido, y, detalle.getOpciones());
                        }
                    }
                }

                y -= 6;
                y = trazarLinea(contenido, y);
                y -= 12;
                y = escribirLineaDerecha(contenido, FUENTE, 10, y, "Subtotal: $" + venta.getSubtotalFormateado());
                y = escribirLineaDerecha(contenido, FUENTE, 10, y, "Envio: $" + venta.getEnvioFormateado());
                escribirLineaDerecha(contenido, FUENTE_NEGRITA, 12, y, "Total: $" + venta.getTotalFormateado());
            }

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            documento.save(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new ErrorInternoException("Error al generar el PDF del ticket", e);
        }
    }

    private static float escribirTitulo(PDPageContentStream contenido, float y) throws IOException {
        contenido.beginText();
        contenido.setFont(FUENTE_NEGRITA, 16);
        contenido.newLineAtOffset(MARGEN_IZQUIERDO, y);
        contenido.showText("Cafe Don Bosco");
        contenido.endText();
        return y - 20;
    }

    private static float escribirLinea(PDPageContentStream contenido, PDFont fuente, float tamano, float y, String texto)
            throws IOException {
        contenido.beginText();
        contenido.setFont(fuente, tamano);
        contenido.newLineAtOffset(MARGEN_IZQUIERDO, y);
        contenido.showText(sanitizar(texto));
        contenido.endText();
        return y - (tamano + 6);
    }

    private static float escribirLineaDerecha(PDPageContentStream contenido, PDFont fuente, float tamano, float y, String texto)
            throws IOException {
        String limpio = sanitizar(texto);
        float ancho = fuente.getStringWidth(limpio) / 1000 * tamano;
        contenido.beginText();
        contenido.setFont(fuente, tamano);
        contenido.newLineAtOffset(MARGEN_IZQUIERDO + ANCHO_PAGINA - ancho, y);
        contenido.showText(limpio);
        contenido.endText();
        return y - (tamano + 6);
    }

    private static float trazarLinea(PDPageContentStream contenido, float y) throws IOException {
        contenido.setLineWidth(0.5f);
        contenido.moveTo(MARGEN_IZQUIERDO, y);
        contenido.lineTo(MARGEN_IZQUIERDO + ANCHO_PAGINA, y);
        contenido.stroke();
        return y;
    }

    private static float escribirFilaDetalle(PDPageContentStream contenido, float y,
                                              String nombre, String cantidad, String precioUnitario, String subtotal,
                                              boolean encabezado) throws IOException {
        PDFont fuente = encabezado ? FUENTE_NEGRITA : FUENTE;
        float tamano = 9;
        contenido.beginText();
        contenido.setFont(fuente, tamano);
        contenido.newLineAtOffset(MARGEN_IZQUIERDO, y);
        contenido.showText(recortar(sanitizar(nombre), 32));
        contenido.endText();

        escribirColumna(contenido, fuente, tamano, y, MARGEN_IZQUIERDO + 260, cantidad);
        escribirColumna(contenido, fuente, tamano, y, MARGEN_IZQUIERDO + 340, precioUnitario);
        escribirColumna(contenido, fuente, tamano, y, MARGEN_IZQUIERDO + 440, subtotal);

        return y - (tamano + 8);
    }

    private static void escribirColumna(PDPageContentStream contenido, PDFont fuente, float tamano, float y, float x, String texto)
            throws IOException {
        contenido.beginText();
        contenido.setFont(fuente, tamano);
        contenido.newLineAtOffset(x, y);
        contenido.showText(sanitizar(texto));
        contenido.endText();
    }

    private static float escribirOpciones(PDPageContentStream contenido, float y, List<OpcionSeleccionadaResponseDTO> opciones)
            throws IOException {
        StringBuilder texto = new StringBuilder();
        for (OpcionSeleccionadaResponseDTO opcion : opciones) {
            if (texto.length() > 0) {
                texto.append(", ");
            }
            texto.append(opcion.getNombreOpcion());
            if (opcion.getPrecioAdicional() != null && opcion.getPrecioAdicional().signum() > 0) {
                texto.append(" (+$").append(FormatoUtil.moneda(opcion.getPrecioAdicional())).append(")");
            }
        }
        contenido.beginText();
        contenido.setFont(FUENTE, 8);
        contenido.newLineAtOffset(MARGEN_IZQUIERDO + 12, y);
        contenido.showText(sanitizar(recortar(texto.toString(), 55)));
        contenido.endText();
        return y - 11;
    }

    private static String recortar(String texto, int maximo) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1) + "...";
    }

    /**
     * PDType1Font (fuentes base14) solo soporta WinAnsiEncoding: algunos
     * caracteres Unicode que Java si sabe imprimir en HTML/consola, como
     * el espacio fino que el formato "a. m." en espanol inserta entre el
     * punto y la "m", no existen en esa codificacion y showText() los
     * rechaza. Se normalizan a un espacio comun antes de escribir.
     */
    private static String sanitizar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto
                .replace(' ', ' ')
                .replace(' ', ' ')
                .replace(' ', ' ');
    }
}
